package com.jtmelton.asa.analysis.visitors.python;

import com.google.common.base.CharMatcher;
import com.jtmelton.asa.analysis.generated.antlr4.python.PythonParser;
import com.jtmelton.asa.analysis.generated.antlr4.python.PythonParserBaseVisitor;
import com.jtmelton.asa.analysis.utils.PythonAstNodes;
import com.jtmelton.asa.domain.Route;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashSet;


public class EnclosureVerificationVisitor extends PythonParserBaseVisitor<Void> {

  // ensure the path() function is wrapped in the urlpatterns array
  private boolean isWrappingValid = false;

  private String VALID_ARRAY_NAME = "urlpatterns";

  private String VALID_FILENAME = "urls.py";

  @Override
  public Void visitStmt(PythonParser.StmtContext ctx) {

    String sourceFileName = PythonAstNodes.getSourceFileName(ctx);

    // ensure 2 rules hold true
    // 1. the wrapper array statement starts with "urlpatterns"
    // 2. the containing class is urls.py
    if(ctx.getText().startsWith(VALID_ARRAY_NAME) && sourceFileName.endsWith(VALID_FILENAME)) {
      isWrappingValid = true;
    }

    return visitChildren(ctx);
  }

  public boolean isWrappingValid() {
    return isWrappingValid;
  }

}