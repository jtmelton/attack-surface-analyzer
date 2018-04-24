package com.jtmelton.asa.domain;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.Collection;

public class Route {
  private String method;
  private String path;
  private Collection<Parameter> parameters = new ArrayList<>();

  public Route(String method, String path, Collection<Parameter> parameters) {
    this.method = method;
    this.path = path;
    this.parameters = parameters;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Collection<Parameter> getParameters() {
    return parameters;
  }

  public void setParameters(Collection<Parameter> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("method", method)
        .add("path", path)
        .add("parameters", parameters)
        .toString();
  }
}
