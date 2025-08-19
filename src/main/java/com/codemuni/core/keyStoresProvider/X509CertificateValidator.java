package com.codemuni.core.keyStoresProvider;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

public interface X509CertificateValidator {

    boolean isExpired(X509Certificate certificate);

    boolean isNotYetValid(X509Certificate certificate);

    boolean isDigitalSignatureAllowed(X509Certificate certificate);

    boolean isEncryptionAllowed(X509Certificate certificate);

    boolean isSignatureAlgorithmSecure(X509Certificate certificate);

    boolean isSelfSigned(X509Certificate certificate);

    boolean isEndEntity(X509Certificate certificate);

    boolean isRevoked(X509Certificate certificate); // validate revocation status of the certificate using OCSP

    boolean isChainValid(List<X509Certificate> chain, Set<X509Certificate> trustedRoots, int maxChainLength);
}
