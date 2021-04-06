package com.jtmelton.asa.config;

import java.util.Arrays;

public class KnownFramework {

    private String name;
    private String prefix;
    private String[] tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "KnownFramework{" +
                "name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", tags=" + Arrays.toString(tags) +
                '}';
    }
}
