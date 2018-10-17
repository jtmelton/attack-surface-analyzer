package com.jtmelton.asa.analysis.visitors.java.jaxrs;

import com.jtmelton.asa.domain.Parameter;

import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.ClassBodyDeclarationContext;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.QualifiedNameContext;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.AnnotationContext;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParserBaseVisitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RouteAnnotationVisitor extends JavaParserBaseVisitor<Void> {

  private Set<String> methods = new HashSet<>();
  private Set<String> classLevelPaths = new HashSet<>();
  private Set<String> methodLevelPaths = new HashSet<>();
  private Set<Parameter> parameters = new HashSet<>();

  private static final Set<String> HTTP_METHODS =
      new HashSet<>(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "CONNECT"));

  private static final Set<String> PATHS_ANNOTATIONS =
      new HashSet<>(Arrays.asList("Path"));

  private static final Set<String> PARAMETER_ANNOTATIONS =
      new HashSet<>(Arrays.asList("HeaderParam", "PathParam", "QueryParam", "CookieParam", "FormParam", "MatrixParam"));

  @Override
  public Void visitAnnotation(AnnotationContext ctx) {
    QualifiedNameContext qualifiedName = ctx.qualifiedName();
    List<TerminalNode> idents = qualifiedName.IDENTIFIER();

    for(TerminalNode ident : idents) {
      String type = ident.getSymbol().getText();

      if(HTTP_METHODS.contains(type)) {
        methods.add(type);
      }

      if (PATHS_ANNOTATIONS.contains(type) || PARAMETER_ANNOTATIONS.contains(type)) {

        RouteParameterVisitor parameterVisitor = new RouteParameterVisitor();
        ctx.getPayload().accept(parameterVisitor);
        String name = parameterVisitor.getName();

        if (PATHS_ANNOTATIONS.contains(type)) {
          if (isMethodLevelPath(ctx)) {
            methodLevelPaths.add(name);
          } else {
            classLevelPaths.add(name);
          }
        } else if (PARAMETER_ANNOTATIONS.contains(type)) {

          parameters.add(new Parameter(type, name, findType(ctx)));
        }
      }
    }

    return visitChildren(ctx);
  }

  public boolean isMethodLevelPath(AnnotationContext ctx) {
    boolean isMethod = false;
    ParserRuleContext parent = ctx.getParent();
    while (parent.getParent() != null) {
      if(parent instanceof ClassBodyDeclarationContext) {
        ClassBodyDeclarationContext cBodyCtx = (ClassBodyDeclarationContext) parent;
        if(cBodyCtx.memberDeclaration() != null)
        isMethod = true;
        break;
      }
      parent = parent.getParent();
    }
    return isMethod;
  }

  private String findType(AnnotationContext ctx) {
    String type = "";
    ParserRuleContext parent = ctx.getParent();
    while (parent.getParent() != null) {
      TypeSearchVisitor visitor = new TypeSearchVisitor();
      parent.accept(visitor);
      if(visitor.getType() != null) {
        type = visitor.getType();
        break;
      }

      parent = parent.getParent();
    }
    return type;
  }

  public Set<String> getMethods() {
    return methods;
  }

  public Set<String> getClassLevelPaths() {
    return classLevelPaths;
  }

  public Set<String> getMethodLevelPaths() {
    return methodLevelPaths;
  }

  public Set<Parameter> getParameters() {
    return parameters;
  }
}