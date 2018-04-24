package com.jtmelton.asa.analysis;

import com.jtmelton.asa.analysis.generated.antlr4.Java8BaseVisitor;
import com.jtmelton.asa.analysis.generated.antlr4.Java8Parser;
import com.jtmelton.asa.domain.Route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class RouteMethodVisitor extends Java8BaseVisitor<Void> {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private Collection<Route> routes = new ArrayList<>();

  private String classLevelPath = "";

  @Override
  public Void visitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
    RouteAnnotationVisitor routeAnnotationVisitor = new RouteAnnotationVisitor();
    ctx.accept(routeAnnotationVisitor);

    assert routeAnnotationVisitor.getMethods().size() <= 1;

    if(! routeAnnotationVisitor.getClassLevelPaths().isEmpty()) {
      this.classLevelPath = routeAnnotationVisitor.getClassLevelPaths().iterator().next();
    }

    return visitChildren(ctx);
  }

  @Override
  public Void visitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
    RouteAnnotationVisitor visitor = new RouteAnnotationVisitor();
    ctx.accept(visitor);

    if(! visitor.getMethods().isEmpty()) {
      assert visitor.getMethods().size() == 1;
      assert visitor.getMethodLevelPaths().size() <= 1;

      String method = visitor.getMethods().iterator().next();
      String methodLevelPath = (visitor.getMethodLevelPaths().size() > 0) ?
                               visitor.getMethodLevelPaths().iterator().next() : "";

      routes.add(new Route(method, classLevelPath + methodLevelPath, visitor.getParameters()));
    }

    return visitChildren(ctx);
  }

  public Collection<Route> getRoutes() {
    return routes;
  }
}