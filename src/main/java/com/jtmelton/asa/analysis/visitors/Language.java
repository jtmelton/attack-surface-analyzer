package com.jtmelton.asa.analysis.visitors;

public enum Language {
    GOLANG(".go"),
    JAVASCRIPT(".js"),
    JAVA(".java"),
    PYTHON(".py");
    String ext;
    Language(String ext) {
        this.ext = ext;
    }
    public String getExtension() {return this.ext;}
}
