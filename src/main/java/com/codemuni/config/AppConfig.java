package com.codemuni.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppConfig {

    @JsonProperty("ACTIVE_STORE")
    public Map<String, Boolean> activeStore = new HashMap<>();

    @JsonProperty("PKCS11")
    public List<String> pkcs11 = new ArrayList<>();

    @JsonProperty("SOFT_HSM")
    public String softHSM = "";


    @JsonProperty("TIMESTAMP_SERVER")
    public Map<String, String> timestampServer = new HashMap<>();

    @JsonProperty("PROXY")
    public Map<String, String> proxy = new HashMap<>();


    public Map<String, String> getTimestampServer() {
        return timestampServer;
    }

    public void setTimestampServer(Map<String, String> timestampServer) {
        this.timestampServer = timestampServer;
    }

    public Map<String, String> getProxy() {
        return proxy;
    }

    public void setProxy(Map<String, String> proxy) {
        this.proxy = proxy;
    }

    public String getSoftHSM() {
        return softHSM;
    }

    public void setSoftHSM(String softHSM) {
        this.softHSM = softHSM;
    }

    public Map<String, Boolean> getActiveStore() {
        return activeStore;
    }

    public void setActiveStore(Map<String, Boolean> activeStore) {
        this.activeStore = activeStore;
    }

    public List<String> getPkcs11() {
        return pkcs11;
    }

    public void setPkcs11(List<String> pkcs11) {
        this.pkcs11 = pkcs11;
    }
}
