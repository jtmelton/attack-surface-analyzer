package com.jtmelton.asa.analysis.visitors.java.jaxrs;

import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParserBaseVisitor;
import static com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.ElementValueContext;

import com.google.common.base.CharMatcher;


public class RouteParameterVisitor extends JavaParserBaseVisitor<Void> {

  private String name = "";

  @Override
  public Void visitElementValue(ElementValueContext elementValueContext) {
    this.name = stripQuotes(elementValueContext.getText());
    return visitChildren(elementValueContext);
  }

  private String stripQuotes(String value) {
    return CharMatcher.is('\"').trimFrom(value);
  }

  public String getName() {
    return name;
  }

}