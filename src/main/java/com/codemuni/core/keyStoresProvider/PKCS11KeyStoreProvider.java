package com.codemuni.core.keyStoresProvider;

import com.codemuni.exceptions.*;
import com.codemuni.gui.TokenPinCallbackHandler;
import com.codemuni.model.KeystoreAndCertificateInfo;
import com.codemuni.utils.AppConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.security.pkcs11.SunPKCS11;
import sun.security.pkcs11.wrapper.*;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PKCS#11 KeyStore provider implementation with persistent session support.
 * PIN is only requested once per app lifecycle unless explicitly logged out/reset.
 */
public final class PKCS11KeyStoreProvider implements KeyStoreProvider {

    private static final Log LOG = LogFactory.getLog(PKCS11KeyStoreProvider.class);

    private static final String PKCS11_TYPE = "PKCS11";
    private static final Provider BC_PROVIDER = new BouncyCastleProvider();

    private final List<String> pkcs11LibPathsToBeLoadPublicKey;
    private final Map<String, String> serialToAlias = new ConcurrentHashMap<>();

    private volatile SunPKCS11 sunPKCS11Provider;
    private volatile KeyStore keyStore;

    private String certificateSerialNumber; // hex string
    private String tokenSerialNumber;       // token info serial string
    private String pkcs11LibPath;           // PKCS#11 library path

    private boolean loggedIn = false; // NEW: Track session state

    public PKCS11KeyStoreProvider(List<String> pkcs11LibPaths) {
        this.pkcs11LibPathsToBeLoadPublicKey = Objects.requireNonNull(pkcs11LibPaths);
    }

    // -------------------- Internal Helpers --------------------

    private static long findSlotByTokenSerial(String libPath, String desiredSerial)
            throws IncorrectPINException, TokenOrHsmNotFoundException, KeyStoreInitializationException {
        try {
            PKCS11 pkcs11 = PKCS11.getInstance(libPath, "C_GetFunctionList", null, false);
            for (long slot : pkcs11.C_GetSlotList(true)) {
                CK_TOKEN_INFO info = pkcs11.C_GetTokenInfo(slot);
                String serial = new String(info.serialNumber).trim();
                if (serial.equalsIgnoreCase(desiredSerial.trim())) {
                    return slot;
                }
            }
            throw new TokenOrHsmNotFoundException("Token with serial " + desiredSerial + " not found in library: " + libPath);
        } catch (PKCS11Exception e) {
            LOG.error("PKCS#11 error: " + e.getMessage(), e);
            throw translatePKCS11Error(e);
        } catch (IOException e) {
            throw new KeyStoreInitializationException("Unable to load PKCS#11 library from path: " + libPath, e);
        }
    }

    private static PKCS11OperationException translatePKCS11Error(PKCS11Exception e) throws IncorrectPINException {
        int code = (int) e.getErrorCode();
        switch (code) {
            case (int) PKCS11Constants.CKR_SLOT_ID_INVALID:
                return new PKCS11OperationException("Invalid slot ID.", e);
            case (int) PKCS11Constants.CKR_TOKEN_NOT_PRESENT:
                return new PKCS11OperationException("No token present in the slot.", e);
            case (int) PKCS11Constants.CKR_TOKEN_NOT_RECOGNIZED:
                return new PKCS11OperationException("Unrecognized token in slot.", e);
            case (int) PKCS11Constants.CKR_PIN_INCORRECT:
                throw new IncorrectPINException("Incorrect PIN.", e);
            case (int) PKCS11Constants.CKR_DEVICE_REMOVED:
                return new PKCS11OperationException("Cryptographic device removed during operation.", e);
            default:
                return new PKCS11OperationException("PKCS#11 error: " + e.getMessage(), e);
        }
    }

    private static void clearPassword(PasswordCallback passwordCallback) {
        if (passwordCallback.getPassword() != null) {
            Arrays.fill(passwordCallback.getPassword(), '\0');
        }
        passwordCallback.clearPassword();
    }

    // -------------------- Public API --------------------

    // Add these helpers anywhere in the class (e.g., near other helpers)
    private static Throwable rootCause(Throwable t) {
        Throwable cur = t, next;
        while (cur != null && (next = cur.getCause()) != null && next != cur) cur = next;
        return cur;
    }

    private static PKCS11Exception findPkcs11Cause(Throwable t) {
        while (t != null) {
            if (t instanceof PKCS11Exception) return (PKCS11Exception) t;
            t = t.getCause();
        }
        return null;
    }

    private static boolean isIncorrectPin(Throwable t) {
        PKCS11Exception ex = findPkcs11Cause(t);
        return ex != null && ex.getErrorCode() == PKCS11Constants.CKR_PIN_INCORRECT;
    }

    private static boolean isPinLocked(Throwable t) {
        PKCS11Exception ex = findPkcs11Cause(t);
        return ex != null && ex.getErrorCode() == PKCS11Constants.CKR_PIN_LOCKED;
    }

    @Override
    public List<KeystoreAndCertificateInfo> loadCertificates() {
        if (pkcs11LibPathsToBeLoadPublicKey.isEmpty()) return Collections.emptyList();

        List<KeystoreAndCertificateInfo> result = new ArrayList<>();
        for (String libPath : pkcs11LibPathsToBeLoadPublicKey) {
            try {
                enumerateLibraryCertificates(libPath, result::add);
            } catch (Exception ex) {
                LOG.warn("Unable to read certificates from PKCS#11 library: " + libPath, ex);
            }
        }
        return result;
    }

    /**
     * Explicit login method — prompts for PIN only if not already logged in.
     */
    private void enumerateLibraryCertificates(String libPath, java.util.function.Consumer<KeystoreAndCertificateInfo> consumer)
            throws Exception {
        PKCS11 pkcs11 = PKCS11.getInstance(libPath, "C_GetFunctionList", null, false);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        for (long slot : pkcs11.C_GetSlotList(true)) {
            long session = 0L;
            try {
                CK_TOKEN_INFO tokenInfo = pkcs11.C_GetTokenInfo(slot);
                String tokenSerial = new String(tokenInfo.serialNumber).trim();

                session = pkcs11.C_OpenSession(slot, PKCS11Constants.CKF_SERIAL_SESSION, null, null);
                CK_ATTRIBUTE[] template = {new CK_ATTRIBUTE(PKCS11Constants.CKA_CLASS, PKCS11Constants.CKO_CERTIFICATE)};

                pkcs11.C_FindObjectsInit(session, template);
                while (true) {
                    long[] objects = pkcs11.C_FindObjects(session, 10);
                    if (objects == null || objects.length == 0) break;
                    for (long obj : objects) {
                        CK_ATTRIBUTE[] attrs = {new CK_ATTRIBUTE(PKCS11Constants.CKA_VALUE)};
                        pkcs11.C_GetAttributeValue(session, obj, attrs);
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(attrs[0].getByteArray())) {
                            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(bais);
                            consumer.accept(new KeystoreAndCertificateInfo(cert, AppConstants.PKCS11_KEY_STORE, tokenSerial, libPath));
                        }
                    }
                }
                pkcs11.C_FindObjectsFinal(session);
            } finally {
                if (session != 0L) {
                    try {
                        pkcs11.C_CloseSession(session);
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }

    public void setCertificateSerialNumber(String certificateSerialNumber) {
        this.certificateSerialNumber = certificateSerialNumber;
    }

    public void setPkcs11LibPath(String pkcs11LibPath) {
        this.pkcs11LibPath = pkcs11LibPath;
    }

    public void setTokenSerialNumber(String tokenSerialNumber) {
        this.tokenSerialNumber = tokenSerialNumber;
    }

    /**
     * Explicit login method — prompts for PIN with up to 3 retries on CKR_PIN_INCORRECT.
     */
    public synchronized void login(TokenPinCallbackHandler pinHandler)
            throws IncorrectPINException, KeyStoreException, IOException,
            UnsupportedCallbackException, CertificateException, NoSuchAlgorithmException {

        if (loggedIn && keyStore != null) {
            LOG.info("Already logged in — reusing existing session.");
            return;
        }

        Objects.requireNonNull(pkcs11LibPath, "PKCS#11 library path must be set.");
        Objects.requireNonNull(tokenSerialNumber, "Token serial number must be set.");

        long slot = findSlotByTokenSerial(pkcs11LibPath, tokenSerialNumber);

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BC_PROVIDER);
        }

        final int maxRetries = 3;

        for (int attempt = 1; true; attempt++) {
            // Ensure any previous provider is removed before a fresh attempt
            if (sunPKCS11Provider != null) {
                try {
                    Security.removeProvider(sunPKCS11Provider.getName());
                } catch (Exception ignore) {
                }
                sunPKCS11Provider = null;
            }

            String config = String.format(Locale.ROOT,
                    "name=PKCS11-%d\nlibrary=%s\nslot=%d", slot, pkcs11LibPath, slot);
            sunPKCS11Provider = new SunPKCS11(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));
            Security.addProvider(sunPKCS11Provider);

            KeyStore ks = KeyStore.getInstance(PKCS11_TYPE, sunPKCS11Provider);

            PasswordCallback passwordCallback = new PasswordCallback("Enter PIN:", true);
            pinHandler.handle(new Callback[]{passwordCallback});

            if (pinHandler.isCancelled()) {
                clearPassword(passwordCallback);
                throw new UserCancelledPasswordEntryException("User cancelled PIN entry.");
            }

            try {
                ks.load(null, passwordCallback.getPassword()); // <-- may throw on wrong PIN
                clearPassword(passwordCallback);

                this.keyStore = ks;
                this.loggedIn = true;
                LOG.info("Login successful — session will remain active until logout() or reset().");
                return;

            } catch (IOException e) {
                // Always clear sensitive data
                clearPassword(passwordCallback);

                // Detect exact PKCS#11 reason by walking the cause chain
                if (isPinLocked(e)) {
                    PKCS11Exception pe = findPkcs11Cause(e);
                    LOG.error("PIN is locked on token. No more attempts.");
                    throw new IncorrectPINException("PIN is locked on the token. Please unblock/reset the PIN.", pe);
                }

                if (isIncorrectPin(e)) {
                    int left = maxRetries - attempt;
                    LOG.warn(String.format("Incorrect PIN. Attempt %d of %d", attempt, maxRetries));
                    if (left == 0) {
                        PKCS11Exception pe = findPkcs11Cause(e);
                        throw new IncorrectPINException("Signing aborted — PIN retry limit exceeded.", pe);
                    }
                    // Optional: update the next prompt's label so the user sees remaining tries
                    try {
                        pinHandler.setLabelText("<html><span style='color:red;'>Incorrect PIN. Try again (<b>" + left + "</b> left)</span></html>");
                    } catch (Exception ignore) { /* handler may not support it, safe to ignore */ }

                    // Loop continues: provider will be recreated on next iteration
                    continue;
                }

                // Not a wrong-PIN scenario → rethrow (device removed, token absent, etc.)
                throw e;
            }
        }
    }

    /**
     * Backward compatibility — behaves like login().
     */
    public synchronized void loadKeyStore(TokenPinCallbackHandler pinHandler)
            throws IncorrectPINException, KeyStoreException, IOException,
            UnsupportedCallbackException, CertificateException, NoSuchAlgorithmException {
        login(pinHandler);
    }

    /**
     * Explicit logout — closes session and clears sensitive data.
     */
    public synchronized void logout() {
        try {
            if (sunPKCS11Provider != null) {
                sunPKCS11Provider.logout();
                Security.removeProvider(sunPKCS11Provider.getName());
            }
        } catch (Exception ignored) {
        }
        keyStore = null;
        loggedIn = false;
        sunPKCS11Provider = null;
        serialToAlias.clear();
        LOG.info("Logged out from token — session closed.");
    }

    public synchronized void reset() {
        logout();
        certificateSerialNumber = null;
        tokenSerialNumber = null;
        pkcs11LibPath = null;
    }

    @Override
    public String getProvider() {
        return (sunPKCS11Provider != null) ? sunPKCS11Provider.getName() : null;
    }

    @Override
    public PrivateKey getPrivateKey()
            throws KeyStoreInitializationException, CertificateNotFoundException,
            PrivateKeyAccessException, NotADigitalSignatureException, KeyStoreException {

        if (keyStore == null) {
            throw new KeyStoreInitializationException("KeyStore not loaded. Call login() first.");
        }
        String alias = getAliasForCertificateSerial();
        try {
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
            if (privateKey == null) {
                throw new PrivateKeyAccessException("No private key for alias: " + alias);
            }
            return privateKey;
        } catch (UnrecoverableKeyException e) {
            throw new PrivateKeyAccessException("Unable to access private key: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreInitializationException("Unsupported algorithm: " + e.getMessage(), e);
        }
    }

    @Override
    public X509Certificate getCertificate()
            throws KeyStoreInitializationException, CertificateNotFoundException,
            NotADigitalSignatureException, KeyStoreException {

        if (keyStore == null) {
            throw new KeyStoreInitializationException("KeyStore not loaded. Call login() first.");
        }
        Certificate cert = keyStore.getCertificate(getAliasForCertificateSerial());
        if (!(cert instanceof X509Certificate)) {
            throw new NotADigitalSignatureException("Certificate is not X.509.");
        }
        return (X509Certificate) cert;
    }

    @Override
    public Certificate[] getCertificateChain() throws KeyStoreException {
        if (keyStore == null) {
            throw new KeyStoreException("KeyStore not loaded. Call login() first.");
        }
        Certificate[] chain = keyStore.getCertificateChain(getAliasForCertificateSerial());
        if (chain == null || chain.length == 0) {
            throw new KeyStoreException("No certificate chain found.");
        }
        return chain;
    }

    private String getAliasForCertificateSerial() {
        if (certificateSerialNumber == null) {
            throw new IllegalArgumentException("Certificate serial number must be set first.");
        }
        return serialToAlias.computeIfAbsent(certificateSerialNumber, serial -> {
            try {
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    Certificate cert = keyStore.getCertificate(alias);
                    if (cert instanceof X509Certificate) {
                        String serialHex = ((X509Certificate) cert).getSerialNumber().toString(16);
                        if (serialHex.equalsIgnoreCase(serial)) {
                            return alias;
                        }
                    }
                }
                return null;
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
