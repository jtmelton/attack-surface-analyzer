package com.jtmelton.asa.analysis;

import com.jtmelton.asa.analysis.generated.antlr4.ecmascript6.JavaScriptLexer;
import com.jtmelton.asa.analysis.generated.antlr4.ecmascript6.JavaScriptParser;
import com.jtmelton.asa.analysis.generated.antlr4.java8.Java8Lexer;
import com.jtmelton.asa.analysis.generated.antlr4.java8.Java8Parser;
import com.jtmelton.asa.analysis.visitors.IRouteVisitor;
import com.jtmelton.asa.analysis.visitors.javascript.express.ExpressRouteVisitor;
import com.jtmelton.asa.analysis.visitors.java.jaxrs.RouteMethodVisitor;
import com.jtmelton.asa.domain.Route;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteAnalyzer {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final Collection<String> exclusions = new ArrayList<>();

  private Collection<Route> routes = new ArrayList<>();

  private final boolean parserStderr;

  public RouteAnalyzer(Collection<String> userExclusions, boolean parserStderr) {
    exclusions.add(".+node_modules.+");
    exclusions.addAll(userExclusions);

    this.parserStderr = parserStderr;
  }

  public void analyze(File sourceDirectory) {
    String sourcePath = sourceDirectory.getAbsolutePath();

    final Collection<Path> paths = new ArrayList<>();

    try {
      Files.walk(Paths.get(sourcePath))
          .filter(Files::isRegularFile)
          .filter(acceptedExts())
          .filter(exclude())
          .forEach(paths::add);

      for(Path path : paths) {
        scan(path);
      }

    } catch (IOException e) {
      throw new IllegalStateException("Failure scanning files.", e);
    }
  }

  private void scan(Path path) throws IOException {
    String fileName = path.toAbsolutePath().toString();

    Language lang = getLanguage(fileName);

    Lexer lexer = getLexer(fileName, lang);

    CommonTokenStream tokens = new CommonTokenStream(lexer);
    tokens.fill(); // load all and check time

    // Create a parser that reads from the scanner
    Parser parser = getParser(tokens, lang);

    if(!parserStderr) {
      parser.removeErrorListeners();
    }

    // start parsing at the compilationUnit rule
    RuleContext ruleContext = getRuleContext(parser, lang);

    for(IRouteVisitor visitor : getRouteVisitors(lang, fileName)) {
      ruleContext.accept((AbstractParseTreeVisitor) visitor);
      routes.addAll(visitor.getRoutes());
    }
  }

  private Language getLanguage(String fileName) {
    String ext = fileName.substring(fileName.lastIndexOf("."));
    for(Language language : Language.values()) {
      if (language.ext.equals(ext)) {
        return language;
      }
    }

    return Language.JAVA;
  }

  private Lexer getLexer(String filename, Language lang) throws IOException {
    Lexer lexer;

    switch (lang) {
      case JAVASCRIPT:
        lexer = new JavaScriptLexer(CharStreams.fromFileName(filename));
      break;
      default:
        lexer = new Java8Lexer(CharStreams.fromFileName(filename));
        break;
    }

    return lexer;
  }

  private Parser getParser(CommonTokenStream tokens, Language lang) {
    Parser parser;

    switch(lang) {
      case JAVASCRIPT:
        parser = new JavaScriptParser(tokens);
        break;
      default:
        parser = new Java8Parser(tokens);
        break;
    }

    return parser;
  }

  private Collection<IRouteVisitor> getRouteVisitors(Language lang, String fileName) {
    Collection<IRouteVisitor> visitors = new ArrayList<>();
    switch (lang) {
      case JAVASCRIPT:
        visitors.add(new ExpressRouteVisitor(fileName));
        break;
      case JAVA:
        visitors.add(new RouteMethodVisitor(fileName));
        break;
    }

    return visitors;
  }

  private RuleContext getRuleContext(Parser parser, Language lang) {
    RuleContext ruleContext;

    switch (lang) {
      case JAVASCRIPT:
        ruleContext = ((JavaScriptParser) parser).program();
        break;
      default:
        ruleContext = ((Java8Parser) parser).compilationUnit();
        break;
    }

    return ruleContext;
  }

  public Collection<Route> getRoutes() {
    return routes;
  }

  private Predicate<Path> acceptedExts() {
    return p -> {
      boolean result = p.getFileName().toString().endsWith(Language.JAVASCRIPT.ext);
      result = result || p.getFileName().toString().endsWith(Language.JAVA.ext);

      return result;
    };
  }

  private Predicate<Path> exclude() {
    return p -> {
      for(String exclusion : exclusions) {
        Pattern pattern = Pattern.compile(exclusion);
        Matcher matcher = pattern.matcher(p.toString());

        if(matcher.matches()) {
          return false;
        }
      }

      return true;
    };
  }

  private enum Language {
    JAVASCRIPT(".js"),
    JAVA(".java");
    String ext;
    Language(String ext) {
      this.ext = ext;
    }
  }
}