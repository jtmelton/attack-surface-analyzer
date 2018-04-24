package com.jtmelton.asa.analysis;

import com.jtmelton.asa.analysis.generated.antlr4.Java8BaseVisitor;
import com.jtmelton.asa.analysis.generated.antlr4.Java8Parser;

import com.google.common.base.CharMatcher;

public class RouteParameterVisitor extends Java8BaseVisitor<Void> {

  private String name = "";

  @Override
  public Void visitElementValue(Java8Parser.ElementValueContext elementValueContext) {
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