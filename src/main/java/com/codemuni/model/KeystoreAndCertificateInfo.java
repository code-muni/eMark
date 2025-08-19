package com.codemuni.model;

import java.security.cert.X509Certificate;
import java.util.Objects;

public class KeystoreAndCertificateInfo {
    private final String keystoreName;
    private final String tokenSerial;
    private final String pkcs11Path;
    private final String pfxFilePath;
    private X509Certificate certificate;

    // Constructor for PKCS11 and Windows keystores
    public KeystoreAndCertificateInfo(X509Certificate certificate, String keystoreName, String tokenSerial, String pkcs11Path) {
        this.certificate = Objects.requireNonNull(certificate, "certificate must not be null");
        this.keystoreName = keystoreName;
        this.tokenSerial = tokenSerial;
        this.pkcs11Path = pkcs11Path;
        this.pfxFilePath = null;
    }

    // Constructor for PFX keystore (certificate loaded later)
    public KeystoreAndCertificateInfo(String keystoreName, String pfxFilePath) {
        this.keystoreName = keystoreName;
        this.pfxFilePath = Objects.requireNonNull(pfxFilePath, "pfxFile must not be null");
        this.tokenSerial = null;
        this.pkcs11Path = null;
        this.certificate = null; // Will be set later
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public String getCertificateSerial() {
        return certificate != null ? certificate.getSerialNumber().toString(16) : null;
    }

    public String getTokenSerial() {
        return tokenSerial;
    }

    public String getKeystoreName() {
        return keystoreName;
    }

    public String getPkcs11Path() {
        return pkcs11Path;
    }

    public String getPfxFilePath() {
        return pfxFilePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeystoreAndCertificateInfo)) return false;
        KeystoreAndCertificateInfo that = (KeystoreAndCertificateInfo) o;

        // If both have certificates, compare them
        if (this.certificate != null && that.certificate != null) {
            return Objects.equals(this.certificate.getSerialNumber(), that.certificate.getSerialNumber()) &&
                    Objects.equals(this.certificate.getIssuerX500Principal(), that.certificate.getIssuerX500Principal());
        }

        // Otherwise, compare by keystore name + PFX file (for PFX)
        return Objects.equals(this.keystoreName, that.keystoreName) &&
                Objects.equals(this.pfxFilePath, that.pfxFilePath);
    }

    @Override
    public int hashCode() {
        if (certificate != null) {
            return Objects.hash(certificate.getSerialNumber(), certificate.getIssuerX500Principal());
        }
        return Objects.hash(keystoreName, pfxFilePath);
    }

    @Override
    public String toString() {
        if (certificate != null) {
            return "CertificateInfo{" +
                    "serial=" + getCertificateSerial() +
                    ", issuer=" + certificate.getIssuerX500Principal().getName() +
                    ", keystoreName='" + keystoreName + '\'' +
                    ", tokenSerial='" + tokenSerial + '\'' +
                    ", pkcs11Path='" + pkcs11Path + '\'' +
                    '}';
        } else {
            return "CertificateInfo{pfxFile='" + pfxFilePath + "', keystoreName='" + keystoreName + "'}";
        }
    }
}
