package com.jtmelton.asa.analysis.visitors.java.jaxrs;

import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.TypeTypeContext;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParserBaseVisitor;

public class TypeSearchVisitor extends JavaParserBaseVisitor<Void> {

  private String type = null;

  @Override
  public Void visitTypeType(TypeTypeContext ctx) {
    this.type = ctx.getText();

    return visitChildren(ctx);
  }

  public String getType() {
    return type;
  }
}