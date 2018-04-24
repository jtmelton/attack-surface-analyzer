package com.jtmelton.asa.analysis;

import com.jtmelton.asa.analysis.generated.antlr4.Java8Lexer;
import com.jtmelton.asa.analysis.generated.antlr4.Java8Parser;
import com.jtmelton.asa.domain.Route;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

public class RouteAnalyzer {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private Collection<Route> routes = new ArrayList<>();

  public void analyze(File sourceDirectory) {

    String sourcePath = sourceDirectory.getAbsolutePath();

    final Collection<Path> paths = new ArrayList<>();

    try {

      Files.walk(Paths.get(sourcePath))
          .filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().endsWith(".java"))
          .forEach(paths::add);

      for(Path path : paths) {
        scan(path);
      }

    } catch (IOException e) {
      throw new IllegalStateException("Failure scanning tarball.", e);
    }

  }

  private void scan(Path path) throws IOException {
    String filename = path.toAbsolutePath().toString();

    Lexer lexer = new Java8Lexer(new ANTLRFileStream(filename));

    CommonTokenStream tokens = new CommonTokenStream(lexer);
    tokens.fill(); // load all and check time

    // Create a parser that reads from the scanner
    Java8Parser parser = new Java8Parser(tokens);

    // start parsing at the compilationUnit rule
    Java8Parser.CompilationUnitContext compilationUnit = parser.compilationUnit();

    RouteMethodVisitor visitor = new RouteMethodVisitor();

    compilationUnit.accept(visitor);

    routes.addAll(visitor.getRoutes());
  }

  public Collection<Route> getRoutes() {
    return routes;
  }
}