package com.jtmelton.asa.analysis;

import com.jtmelton.asa.analysis.generated.antlr4.Java8BaseVisitor;
import com.jtmelton.asa.analysis.generated.antlr4.Java8Parser;

public class TypeSearchVisitor extends Java8BaseVisitor<Void> {

  private String type = null;

  @Override public Void visitUnannType(Java8Parser.UnannTypeContext ctx) {
    this.type = ctx.getText();

    return visitChildren(ctx);
  }

  public String getType() {
    return type;
  }
}