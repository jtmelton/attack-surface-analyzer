package com.jtmelton.asa.analysis.utils;

import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser;
import com.jtmelton.asa.analysis.generated.antlr4.python.PythonParser;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;

public class PythonAstNodes {

  public static PythonParser.RootContext getRootContext(ParserRuleContext ctx) {
    ParserRuleContext parent = ctx.getParent();

    while(parent != null && !(parent instanceof PythonParser.RootContext)) {
      parent = parent.getParent();
    }

    return (PythonParser.RootContext) parent;
  }

  public static PythonParser.StmtContext getStatementContext(PythonParser.ExprContext ctx) {
    ParserRuleContext parent = ctx.getParent();

    while(parent != null && !(parent instanceof PythonParser.StmtContext)) {
      parent = parent.getParent();
    }

    return (PythonParser.StmtContext) parent;
  }

  public static String getSourceFileName(ParserRuleContext ctx) {
    PythonParser.RootContext rootContext = getRootContext(ctx);

    CommonToken token = (CommonToken) rootContext.EOF().getSymbol();
    return token.getTokenSource().getSourceName();
  }

}
