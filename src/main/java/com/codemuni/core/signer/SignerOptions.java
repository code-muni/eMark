package com.codemuni.core.signer;

import com.itextpdf.text.pdf.security.TSAClient;

import java.security.PrivateKey;
import java.security.cert.Certificate;

public class SignerOptions {

    private String provider;
    private PrivateKey privateKey;
    private Certificate[] certificateChain;


    public SignerOptions(String provider, PrivateKey privateKey, Certificate[] certificateChain) {
        this.provider = provider;
        this.privateKey = privateKey;
        this.certificateChain = certificateChain;
    }

    public SignerOptions() {
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public Certificate[] getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(Certificate[] certificateChain) {
        this.certificateChain = certificateChain;
    }
}
