package com.jtmelton.asa.analysis;

import com.jtmelton.asa.analysis.generated.antlr4.ecmascript6.JavaScriptLexer;
import com.jtmelton.asa.analysis.generated.antlr4.ecmascript6.JavaScriptParser;
import com.jtmelton.asa.analysis.generated.antlr4.golang.GoLexer;
import com.jtmelton.asa.analysis.generated.antlr4.golang.GoParser;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaLexer;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser;
import com.jtmelton.asa.analysis.generated.antlr4.python.PythonLexer;
import com.jtmelton.asa.analysis.generated.antlr4.python.PythonParser;
import com.jtmelton.asa.analysis.visitors.IBaseVisitor;
import com.jtmelton.asa.analysis.visitors.Language;
import com.jtmelton.asa.analysis.visitors.Phase;
import com.jtmelton.asa.analysis.visitors.golang.GoRouteVisitor;
import com.jtmelton.asa.analysis.visitors.java.frameworkdetection.FrameworkDetectionVisitor;
import com.jtmelton.asa.analysis.visitors.java.jaxrs.JaxRsVisitor;
import com.jtmelton.asa.analysis.visitors.java.spring.SpringVisitor;
import com.jtmelton.asa.analysis.visitors.javascript.express.ExpressRouteVisitor;
import com.jtmelton.asa.analysis.visitors.python.DjangoVisitor;
import com.jtmelton.asa.config.Configuration;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.HOURS;

public abstract class BaseAnalyzer {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final Collection<String> exclusions = new ArrayList<>();

    private final Collection<IBaseVisitor> visitors = new ArrayList<>();

    private final Set<Language> acceptedLangs = new HashSet<>();

    private final BlockingQueue<Path> paths = new LinkedBlockingQueue<>();

    private final AtomicInteger filesProcessed = new AtomicInteger();

    private final int threads;

    private int pathCount;

    protected boolean parserStderr;

    private boolean visitorsRegistered = false;

    public BaseAnalyzer(int threads) {
        this.threads = threads;
    }

    public BlockingQueue<Path> getPaths(File sourceDirectory) {
        String sourcePath = sourceDirectory.getAbsolutePath();

        try {
            log.info("Collecting paths");
            Files.walk(Paths.get(sourcePath))
                    .filter(Files::isRegularFile)
                    .filter(acceptedExts())
                    .filter(exclude())
                    .forEach(paths::add);

            pathCount = paths.size();

            log.info("{} paths collected", paths.size());

        } catch (IOException e) {
            throw new IllegalStateException("Failure scanning files.", e);
        }

        return paths;
    }

    // scan all phases
    protected void scan(BlockingQueue<Path> paths) {
        try {
            scan(paths, Phase.ONE);
            scan(paths, Phase.TWO);
            scan(paths, Phase.THREE);
        } catch(InterruptedException ie) {
            log.error("Process interrupted while waiting for threads to complete", ie);
        }
    }

    protected void scan(BlockingQueue<Path> paths, Phase phase) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        filesProcessed.set(0);
        log.info("Visitation phase {}", phase);
        for(int i = 0;i < threads;i++) {
            executor.submit(() -> {
                while(true) {
                    Path path;
                    try {
                        path = paths.poll(3, TimeUnit.SECONDS);
                    } catch (InterruptedException ie) {
                        break;
                    }

                    if(path == null) {
                        break;
                    }
                    scan(path, phase);
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(24, HOURS);
    }

    protected void scan(Path path, Phase phase) {
        String fileName = path.toAbsolutePath().toString();

        Language lang = getLanguage(fileName);

        Collection<IBaseVisitor> langVisitors = getVisitors(lang);

        boolean proceed = false;
        for (IBaseVisitor visitor : langVisitors) {
            proceed = proceed || visitor.acceptedPhase(phase);
        }

        if (!proceed) {
            return;
        }

        Lexer lexer;
        try {
            lexer = getLexer(fileName, lang);
        } catch (IOException ioe) {
            log.warn("lexer failed for language {}.", lang.name(), ioe);
            return;
        }

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill(); // load all and check time

        // Create a parser that reads from the scanner
        Parser parser = getParser(tokens, lang);

        if (!parserStderr) {
            parser.removeErrorListeners();
        }

        // start parsing at the compilationUnit rule
        RuleContext ruleContext = getRuleContext(parser, lang);

        for (IBaseVisitor visitor : langVisitors) {
            visitor.setPhase(phase);
            ruleContext.accept((AbstractParseTreeVisitor) visitor);
        }

        int processed = filesProcessed.incrementAndGet();
        log.info("Phase {}, Processed {} out of {}", phase, processed, pathCount);
    }

    private Collection<IBaseVisitor> getVisitors(Language lang) {
        Collection<IBaseVisitor> langVisitors = new ArrayList<>();

        for (IBaseVisitor visitor : visitors) {
            if (visitor.acceptedLang(lang)) {
                langVisitors.add(visitor);
            }
        }

        return langVisitors;
    }

    private Language getLanguage(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf("."));
        for (Language language : Language.values()) {
            if (language.getExtension().equals(ext)) {
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
            case PYTHON:
                lexer = new PythonLexer(CharStreams.fromFileName(filename));
                break;
            case GOLANG:
                lexer = new GoLexer(CharStreams.fromFileName(filename));
                break;
            default:
                lexer = new JavaLexer(CharStreams.fromFileName(filename));
                break;
        }

        return lexer;
    }

    private Parser getParser(CommonTokenStream tokens, Language lang) {
        Parser parser;

        switch (lang) {
            case JAVASCRIPT:
                parser = new JavaScriptParser(tokens);
                break;
            case PYTHON:
                parser = new PythonParser(tokens);
                break;
            case GOLANG:
                parser = new GoParser(tokens);
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
            case PYTHON:
                ruleContext = ((PythonParser) parser).root();
                break;
            case GOLANG:
                ruleContext = ((GoParser) parser).sourceFile();
                break;
            default:
                ruleContext = ((JavaParser) parser).compilationUnit();
                break;
        }

        return ruleContext;
    }

    private Predicate<Path> acceptedExts() {
        return p -> {
            for (Language lang : acceptedLangs) {
                if (p.getFileName().toString().endsWith(lang.getExtension())) {
                    return true;
                }
            }
            return false;
        };
    }

    private Predicate<Path> exclude() {
        return p -> {
            for (String exclusion : exclusions) {
                Pattern pattern = Pattern.compile(exclusion);
                Matcher matcher = pattern.matcher(p.toString());

                if (matcher.matches()) {
                    return false;
                }
            }

            return true;
        };
    }

    protected synchronized void registerVisitors(Configuration configuration) {
        // only register visitors once
        if (!visitorsRegistered) {
            if (configuration.isEnableGolang()) {
                registerVisitor(new GoRouteVisitor());
                acceptedLangs.add(Language.GOLANG);
            }
            if (configuration.isEnableJsExpress()) {
                registerVisitor(new ExpressRouteVisitor());
                acceptedLangs.add(Language.JAVASCRIPT);
            }
            if (configuration.isEnableJavaJaxRs()) {
                registerVisitor(new JaxRsVisitor());
                acceptedLangs.add(Language.JAVA);
            }
            if (configuration.isEnableJavaSpring()) {
                registerVisitor(new SpringVisitor());
                acceptedLangs.add(Language.JAVA);
            }
            if (configuration.isEnablePythonDjango()) {
                registerVisitor(new DjangoVisitor());
                acceptedLangs.add(Language.PYTHON);
            }

            if (configuration.isEnableJavaFrameworkDetection()) {
                registerVisitor(new FrameworkDetectionVisitor(configuration));
                acceptedLangs.add(Language.JAVA);
            }
        }
        this.visitorsRegistered = true;
    }

    private void registerVisitor(IBaseVisitor visitor) {
        visitors.add(visitor);
    }

    protected Collection<IBaseVisitor> getVisitors() {
        return visitors;
    }

}