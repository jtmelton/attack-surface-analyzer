package com.jtmelton.asa.analysis.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class Settings {

  public static final String VISITOR_JAXRS = "visitor.java.jaxrs";
  public static final String VISITOR_SPRING = "visitor.java.spring";
  public static final String VISITOR_EXPRESS = "visitor.js.express";
  public static final String VISITOR_DJANGO = "visitor.python.django";
  public static final String VISITOR_JAVA_FRAMEWORK_DETECTION = "visitor.java.frameworkdetection";

  private final Properties props = new Properties();

  public Settings() throws IOException {
    ClassLoader loader = getClass().getClassLoader();
    InputStream is = loader.getResourceAsStream("visitors.properties");
    init(is);
  }

  public Settings(String propsLocation) throws IOException {
    InputStream is = new FileInputStream(propsLocation);
    init(is);
  }

  public void init(InputStream is) throws IOException {
    try {
      props.load(is);
    } finally {
      if(is != null) {
        is.close();
      }
    }
  }

  public boolean getPropBool(String key) {
    String value = props.getProperty(key);
    return Boolean.parseBoolean(value);
  }
}
