package com.jtmelton.asa.analysis.visitors.java.spring;

import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.ClassBodyDeclarationContext;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParserBaseVisitor;
import com.jtmelton.asa.analysis.utils.JavaAstNodes;
import com.jtmelton.asa.analysis.visitors.IRouteVisitor;
import com.jtmelton.asa.analysis.visitors.Language;
import com.jtmelton.asa.analysis.visitors.Phase;
import com.jtmelton.asa.domain.Route;

import java.util.ArrayList;
import java.util.Collection;

public class SpringVisitor extends JavaParserBaseVisitor<Void> implements IRouteVisitor {

  private final Collection<Route> routes = new ArrayList<>();

  private String classLevelPath = "";

  @Override
  public Void visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
    RouteSpringAnnotationVisitor routeAnnotationVisitor = new RouteSpringAnnotationVisitor();
    ctx.getParent().accept(routeAnnotationVisitor);

    if(! routeAnnotationVisitor.getClassLevelPaths().isEmpty()) {
      this.classLevelPath = routeAnnotationVisitor.getClassLevelPaths().iterator().next();
    }

    return visitChildren(ctx);
  }

  @Override
  public Void visitClassBodyDeclaration(ClassBodyDeclarationContext ctx) {
    RouteSpringAnnotationVisitor visitor = new RouteSpringAnnotationVisitor();
    ctx.accept(visitor);

    if(!visitor.getMethods().isEmpty()) {
      assert visitor.getMethods().size() == 1;
      assert visitor.getMethodLevelPaths().size() <= 1;

      String method = visitor.getMethods().iterator().next();
      String methodLevelPath = (visitor.getMethodLevelPaths().size() > 0) ?
                               visitor.getMethodLevelPaths().iterator().next() : "";

      String fileName = JavaAstNodes.getSourceFileName(ctx);
      routes.add(new Route(fileName, method, classLevelPath + methodLevelPath, visitor.getParameters()));
    }

    return visitChildren(ctx);
  }

  @Override
  public Collection<Route> getRoutes() {
    return routes;
  }

  @Override
  public void setPhase(Phase phase) { }

  @Override
  public boolean acceptedPhase(Phase phase) {
    return Phase.ONE == phase;
  }

  @Override
  public boolean acceptedLang(Language lang) {
    return Language.JAVA == lang;
  }
}