package com.jtmelton.asa.domain;

import com.google.common.base.MoreObjects;

public class Parameter {
  private String category;
  private String name;
  private String dataType;

  public Parameter(String category, String name, String dataType) {
    this.category = category;
    this.name = name;
    this.dataType = dataType;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("category", category)
        .add("name", name)
        .add("dataType", dataType)
        .toString();
  }
}
