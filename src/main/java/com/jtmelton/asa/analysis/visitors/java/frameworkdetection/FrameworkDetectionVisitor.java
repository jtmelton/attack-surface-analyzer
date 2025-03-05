package com.jtmelton.asa.analysis.visitors.java.frameworkdetection;

import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParserBaseVisitor;
import com.jtmelton.asa.analysis.visitors.IFrameworkVisitor;
import com.jtmelton.asa.analysis.visitors.Language;
import com.jtmelton.asa.analysis.visitors.Phase;
import com.jtmelton.asa.config.Configuration;
import com.jtmelton.asa.config.KnownFramework;
import com.jtmelton.asa.domain.Framework;
import com.jtmelton.asa.domain.Package;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FrameworkDetectionVisitor extends JavaParserBaseVisitor<Void> implements IFrameworkVisitor {

    private static Set<String> importedPackages = new HashSet<>();

    private Collection<KnownFramework> knownFrameworks = new HashSet<>();

    private Set<Framework> detectedFrameworks = new HashSet<>();

    private Set<Package> unknownPackages = new HashSet<>();

    public FrameworkDetectionVisitor(Configuration configuration) {
        this.knownFrameworks = configuration.getKnownFrameworks();
    }

    @Override
    public Void visitImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
        String importDecl = ctx.getChild(1).getText();

        // short circuit for empty
        if (importDecl == null || importDecl.trim().length() == 0) {
            return visitChildren(ctx);
        }

        String[] parts = importDecl.split("\\.");

        if (parts.length < 2) {
            // no-op for direct non-packaged imports ... not a framework
            return visitChildren(ctx);
        } else if (parts.length == 3) {
            // com.lib.Class ... should get com.lib and stop
            importedPackages.add(
                    new StringBuilder()
                            .append(parts[0])
                            .append(".")
                            .append(parts[1])
                            .toString());
        } else if (parts.length == 4) {
            // com.lib.sub.Class ... should get com.lib.sub and stop
            importedPackages.add(
                    new StringBuilder()
                            .append(parts[0])
                            .append(".")
                            .append(parts[1])
                            .append(".")
                            .append(parts[2])
                            .toString());
        } else if (parts.length >= 5) {
            // com.lib.sub.another.Class or longer ... should get com.lib.sub.another and stop and depth 4
            importedPackages.add(
                    new StringBuilder()
                            .append(parts[0])
                            .append(".")
                            .append(parts[1])
                            .append(".")
                            .append(parts[2])
                            .append(".")
                            .append(parts[3])
                            .toString());
        }

        return visitChildren(ctx);
    }

    public Set<String> getImportedPackages() {
        return importedPackages;
    }

    public void setImportedPackages(Set<String> importedPackages) {
        this.importedPackages = importedPackages;
    }

    @Override
    public Collection<Framework> getDetectedFrameworks() {
        for (String importedPackage : importedPackages) {
            for (KnownFramework knownFramework : knownFrameworks) {
                if (importedPackage.startsWith(knownFramework.getPrefix())) {
                    detectedFrameworks.add(new Framework(knownFramework.getName()));
                    break;
                }
            }
        }

        return detectedFrameworks;
    }

    public Collection<Package> getUnknownPackages() {
        for (String importedPackage : importedPackages) {
            boolean found = false;

            for (KnownFramework knownFramework : knownFrameworks) {
                if (importedPackage.startsWith(knownFramework.getPrefix())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                unknownPackages.add(new Package(importedPackage));
            }
        }

        return unknownPackages;
    }

    @Override
    public void setPhase(Phase phase) {
    }

    @Override
    public boolean acceptedPhase(Phase phase) {
        return Phase.ONE == phase;
    }

    @Override
    public boolean acceptedLang(Language lang) {
        return Language.JAVA == lang;
    }
}