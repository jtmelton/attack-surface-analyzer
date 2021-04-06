package com.jtmelton.asa.config;

import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

public class Configuration {

    private boolean enableJavaJaxRs;
    private boolean enableJavaSpring;
    private boolean enableJsExpress;
    private boolean enablePythonDjango;
    private boolean enableJavaFrameworkDetection;

    private Collection<KnownFramework> knownFrameworks = new ArrayList<>();

    public boolean isEnableJavaJaxRs() {
        return enableJavaJaxRs;
    }

    public void setEnableJavaJaxRs(boolean enableJavaJaxRs) {
        this.enableJavaJaxRs = enableJavaJaxRs;
    }

    public boolean isEnableJavaSpring() {
        return enableJavaSpring;
    }

    public void setEnableJavaSpring(boolean enableJavaSpring) {
        this.enableJavaSpring = enableJavaSpring;
    }

    public boolean isEnableJsExpress() {
        return enableJsExpress;
    }

    public void setEnableJsExpress(boolean enableJsExpress) {
        this.enableJsExpress = enableJsExpress;
    }

    public boolean isEnablePythonDjango() {
        return enablePythonDjango;
    }

    public void setEnablePythonDjango(boolean enablePythonDjango) {
        this.enablePythonDjango = enablePythonDjango;
    }

    public boolean isEnableJavaFrameworkDetection() {
        return enableJavaFrameworkDetection;
    }

    public void setEnableJavaFrameworkDetection(boolean enableJavaFrameworkDetection) {
        this.enableJavaFrameworkDetection = enableJavaFrameworkDetection;
    }

    public Collection<KnownFramework> getKnownFrameworks() {
        return knownFrameworks;
    }

    public void setKnownFrameworks(Collection<KnownFramework> knownFrameworks) {
        this.knownFrameworks = knownFrameworks;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "enableJavaJaxRs=" + enableJavaJaxRs +
                ", enableJavaSpring=" + enableJavaSpring +
                ", enableJsExpress=" + enableJsExpress +
                ", enablePythonDjango=" + enablePythonDjango +
                ", enableJavaFrameworkDetection=" + enableJavaFrameworkDetection +
                ", knownFrameworks=" + knownFrameworks +
                '}';
    }
}
