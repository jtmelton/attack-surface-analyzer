package com.jtmelton.asa.analysis.visitors.java.jaxrs;

import com.jtmelton.asa.analysis.generated.antlr4.java8.Java8BaseVisitor;
import com.jtmelton.asa.analysis.generated.antlr4.java8.Java8Parser;
import com.jtmelton.asa.domain.Parameter;

import org.antlr.v4.runtime.ParserRuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RouteAnnotationVisitor extends Java8BaseVisitor<Void> {

  private Set<String> methods = new HashSet<>();
  private Set<String> classLevelPaths = new HashSet<>();
  private Set<String> methodLevelPaths = new HashSet<>();
  private Set<Parameter> parameters = new HashSet<>();

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private static final Set<String> HTTP_METHODS =
      new HashSet<>(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "CONNECT"));

  private static final Set<String> PATHS_ANNOTATIONS =
      new HashSet<>(Arrays.asList("Path"));

  private static final Set<String> PARAMETER_ANNOTATIONS =
      new HashSet<>(Arrays.asList("HeaderParam", "PathParam", "QueryParam", "CookieParam", "FormParam", "MatrixParam"));

  @Override public Void visitMarkerAnnotation(Java8Parser.MarkerAnnotationContext ctx) {
    if(ctx != null && ctx.typeName() != null && ctx.typeName().Identifier() != null) {
      String method = ctx.typeName().Identifier().toString();

      if(HTTP_METHODS.contains(method)) {
        methods.add(method);
      }
    }

    return visitChildren(ctx);
  }

  @Override public Void visitSingleElementAnnotation(Java8Parser.SingleElementAnnotationContext ctx) {
    if(ctx != null && ctx.typeName() != null && ctx.typeName().Identifier() != null) {
      String identifier = ctx.typeName().Identifier().toString();

      if(PATHS_ANNOTATIONS.contains(identifier) || PARAMETER_ANNOTATIONS.contains(identifier)) {

        RouteParameterVisitor parameterVisitor = new RouteParameterVisitor();
        ctx.getPayload().accept(parameterVisitor);
        String name = parameterVisitor.getName();

        if (PATHS_ANNOTATIONS.contains(identifier)) {
          if (isMethodLevelPath(ctx)) {
            methodLevelPaths.add(name);
          } else {
            classLevelPaths.add(name);
          }
        } else if (PARAMETER_ANNOTATIONS.contains(identifier)) {

          parameters.add(new Parameter(identifier, name, findType(ctx)));
        }
      }
    }

    return visitChildren(ctx);
  }

  public boolean isMethodLevelPath(Java8Parser.SingleElementAnnotationContext ctx) {
    boolean isMethod = false;
    ParserRuleContext parent = ctx.getParent();
    while (parent.getParent() != null) {
      if(parent instanceof Java8Parser.MethodDeclarationContext) {
        isMethod = true;
        break;
      }
      parent = parent.getParent();
    }
    return isMethod;
  }

  private String findType(Java8Parser.SingleElementAnnotationContext ctx) {
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