package com.jtmelton.asa.analysis;

import com.jtmelton.asa.analysis.generated.antlr4.ecmascript6.JavaScriptLexer;
import com.jtmelton.asa.analysis.generated.antlr4.ecmascript6.JavaScriptParser;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaLexer;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser;
import com.jtmelton.asa.analysis.utils.Settings;
import com.jtmelton.asa.analysis.visitors.IRouteVisitor;
import com.jtmelton.asa.analysis.visitors.IRouteVisitor.Phase;
import com.jtmelton.asa.analysis.visitors.java.jaxrs.JaxRsVisitor;
import com.jtmelton.asa.analysis.visitors.java.netsuite.NSClassVisitor;
import com.jtmelton.asa.analysis.visitors.javascript.express.ExpressRouteVisitor;
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

  private Collection<IRouteVisitor> visitors = new ArrayList<>();

  private Set<Language> acceptedLangs = new HashSet<>();

  private int processed = 0;

  private int pathCount;

  private final boolean parserStderr;

  public RouteAnalyzer(Settings settings, Collection<String> userExclusions, boolean parserStderr) {
    registerVisitors(settings);

    exclusions.add(".+node_modules.+");
    exclusions.addAll(userExclusions);

    this.parserStderr = parserStderr;
  }

  public void analyze(File sourceDirectory) {
    String sourcePath = sourceDirectory.getAbsolutePath();

    final Collection<Path> paths = new ArrayList<>();

    try {
      log.info("Collecting paths");
      Files.walk(Paths.get(sourcePath))
          .filter(Files::isRegularFile)
          .filter(acceptedExts())
          .filter(exclude())
          .forEach(paths::add);

      pathCount = paths.size();


      log.info("{} paths collected", paths.size());

      scan(paths, Phase.ONE);
      scan(paths, Phase.TWO);
      scan(paths, Phase.THREE);

      for(IRouteVisitor visitor : visitors) {
        routes.addAll(visitor.getRoutes());
      }

    } catch (IOException e) {
      throw new IllegalStateException("Failure scanning files.", e);
    }
  }

  private void scan(Collection<Path> paths, Phase phase) throws IOException {
    processed = 0;
    log.info("Visitation phase {}", phase);
    for(Path path : paths) {
      scan(path, phase);
    }
  }

  private void scan(Path path, Phase phase) throws IOException {
    String fileName = path.toAbsolutePath().toString();

    Language lang = getLanguage(fileName);

    Collection<IRouteVisitor> langVisitors = getVisitors(lang);

    boolean proceed = false;
    for(IRouteVisitor visitor : langVisitors) {
      proceed = proceed || visitor.acceptedPhase(phase);
    }

    if(!proceed) {
      log.info("Skipping phase {}", phase);
      return;
    }

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

    for(IRouteVisitor visitor : langVisitors) {
      visitor.setPhase(phase);
      ruleContext.accept((AbstractParseTreeVisitor) visitor);
    }

    processed++;
    log.info("Processed {} out of {}", processed, pathCount);
  }

  private Collection<IRouteVisitor> getVisitors(Language lang) {
    Collection<IRouteVisitor> langVisitors = new ArrayList<>();

    for(IRouteVisitor visitor : visitors) {
      if(visitor.acceptedLang(lang)) {
        langVisitors.add(visitor);
      }
    }

    return langVisitors;
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
        lexer = new JavaLexer(CharStreams.fromFileName(filename));
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
        parser = new JavaParser(tokens);
        break;
    }

    return parser;
  }

  private RuleContext getRuleContext(Parser parser, Language lang) {
    RuleContext ruleContext;

    switch (lang) {
      case JAVASCRIPT:
        ruleContext = ((JavaScriptParser) parser).program();
        break;
      default:
        ruleContext = ((JavaParser) parser).compilationUnit();
        break;
    }

    return ruleContext;
  }

  public Collection<Route> getRoutes() {
    return routes;
  }

  private Predicate<Path> acceptedExts() {
    return p -> {
      for(Language lang : acceptedLangs) {
        if(p.getFileName().toString().endsWith(lang.ext)) {
          return true;
        }
      }
      return false;
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

  private void registerVisitors(Settings settings) {
    if(settings.getPropBool(Settings.VISITOR_EXPRESS)) {
      registerVisitor(new ExpressRouteVisitor());
      acceptedLangs.add(Language.JAVASCRIPT);
    }
    if(settings.getPropBool(Settings.VISITOR_JAXRS)) {
      registerVisitor(new JaxRsVisitor());
      acceptedLangs.add(Language.JAVA);
    }
  }

  public void registerVisitor(IRouteVisitor visitor) {
    visitors.add(visitor);
  }

  public enum Language {
    JAVASCRIPT(".js"),
    JAVA(".java");
    String ext;
    Language(String ext) {
      this.ext = ext;
    }
  }
}