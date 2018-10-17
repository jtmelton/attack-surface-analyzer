package com.jtmelton.asa.analysis.utils;

import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.*;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaAstNodes {

  public static String getClassName(ClassDeclarationContext ctx) {
    return ctx.IDENTIFIER().getSymbol().getText();
  }

  public static List<TerminalNode> getSuperClass(ClassDeclarationContext ctx) {
    if(ctx.typeType() == null) {
      return Collections.EMPTY_LIST;
    }

    return ctx.typeType().classOrInterfaceType().IDENTIFIER();
  }

  public static List<String> getPkgs(ClassDeclarationContext ctx) {
    List<String> packages = new ArrayList<>();

    CompilationUnitContext compUnit = getCompUnit(ctx);
    PackageDeclarationContext packageCtx = compUnit.packageDeclaration();

    if(packageCtx == null) {
      return Collections.EMPTY_LIST;
    }

    QualifiedNameContext qualified = packageCtx.qualifiedName();

    qualified.IDENTIFIER().forEach(n -> packages.add(n.getSymbol().getText()));

    return packages;
  }

  public static String getQualifiedClassName(ClassDeclarationContext ctx) {
    String className = getClassName(ctx);
    List<String> packages = getPkgs(ctx);

    StringBuilder builder = new StringBuilder();

    packages.forEach(p -> {
      builder.append(p);
      builder.append(".");
    });

    builder.append(className);

    return builder.toString();
  }

  public static CompilationUnitContext getCompUnit(ParserRuleContext ctx) {
    ParserRuleContext parent = ctx.getParent();

    while(parent != null && !(parent instanceof CompilationUnitContext)) {
      parent = parent.getParent();
    }

    return (CompilationUnitContext) parent;
  }

  public static String getSourceFileName(ParserRuleContext ctx) {
    CompilationUnitContext compUnit = getCompUnit(ctx);

    CommonToken token = (CommonToken) compUnit.EOF().getSymbol();
    return token.getTokenSource().getSourceName();
  }
}
