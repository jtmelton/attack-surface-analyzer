package com.jtmelton.asa.domain;

import com.google.common.base.MoreObjects;
import org.json.JSONObject;

import java.util.Objects;

public class Framework {
    private String name;

    public Framework(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject toJSON() {
        JSONObject framework = new JSONObject();

        framework.put("name", name);

        return framework;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Framework framework = (Framework) o;
        return Objects.equals(name, framework.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
