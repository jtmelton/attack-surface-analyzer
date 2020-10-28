package com.jtmelton.asa.analysis.visitors.python;

import com.google.common.base.CharMatcher;
import com.jtmelton.asa.analysis.generated.antlr4.python.PythonParser;
import com.jtmelton.asa.analysis.generated.antlr4.python.PythonParserBaseVisitor;


public class RouteParameterVisitor extends PythonParserBaseVisitor<Void> {

  private String name = "";

  @Override
  public Void visitArglist(PythonParser.ArglistContext ctx) {

    if(ctx.getChildCount() > 1) {
      name = stripQuotes(ctx.getChild(0).getText());
    }

    return visitChildren(ctx);
  }

  private String stripQuotes(String value) {
    return CharMatcher.is('\"').trimFrom(value);
  }

  public String getName() {
    return name;
  }

}