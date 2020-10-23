package com.jtmelton.asa.analysis.visitors.java.spring;

import com.google.common.base.CharMatcher;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParserBaseVisitor;

import static com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.ElementValueContext;


public class RouteSpringParameterVisitor extends JavaParserBaseVisitor<Void> {

  private String name = "";

  @Override
  public Void visitElementValuePair(JavaParser.ElementValuePairContext ctx) {
    if (ctx.children.size() == 3 && ctx.getChild(0).getText().equals("value")) {
      this.name = stripQuotes(ctx.getChild(2).getText());
    }
    return super.visitElementValuePair(ctx);
  }

  @Override
  public Void visitElementValue(ElementValueContext elementValueContext) {
    if (this.name.equals("")) {
      this.name = stripQuotes(elementValueContext.getText());
    }
    return visitChildren(elementValueContext);
  }

  private String stripQuotes(String value) {
    return CharMatcher.is('\"').trimFrom(value);
  }

  public String getName() {
    return name;
  }
}