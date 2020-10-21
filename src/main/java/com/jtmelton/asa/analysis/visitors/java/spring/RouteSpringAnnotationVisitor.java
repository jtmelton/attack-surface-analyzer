package com.jtmelton.asa.analysis.visitors.java.spring;

import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.AnnotationContext;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.ClassBodyDeclarationContext;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParser.QualifiedNameContext;
import com.jtmelton.asa.analysis.generated.antlr4.java8.JavaParserBaseVisitor;
import com.jtmelton.asa.domain.Parameter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

import static com.google.common.collect.ImmutableList.*;

public class RouteSpringAnnotationVisitor extends JavaParserBaseVisitor<Void> {

    private final Set<String> methods = new HashSet<>();
    private final Set<String> classLevelPaths = new HashSet<>();
    private final Set<String> methodLevelPaths = new HashSet<>();
    private final Set<Parameter> parameters = new HashSet<>();

    private static final Set<String> PATHS_ANNOTATIONS =
            new HashSet<>(of("RequestMapping"));

    @Override
    public Void visitAnnotation(AnnotationContext ctx) {
        QualifiedNameContext qualifiedName = ctx.qualifiedName();
        List<TerminalNode> idents = qualifiedName.IDENTIFIER();
        for (TerminalNode ident : idents) {
            String type = ident.getSymbol().getText();

            if (PATHS_ANNOTATIONS.contains(type)) {
                methods.add(type);
            }

            if (PATHS_ANNOTATIONS.contains(type)) {

                RouteSpringParameterVisitor parameterVisitor = new RouteSpringParameterVisitor();
                ctx.getPayload().accept(parameterVisitor);
                String name = parameterVisitor.getName();

                if (PATHS_ANNOTATIONS.contains(type)) {
                    if (isMethodLevelPath(ctx)) {
                        methodLevelPaths.add(name);
                    } else {
                        classLevelPaths.add(name);
                    }
                }
            }
        }

        return visitChildren(ctx);
    }

    public boolean isMethodLevelPath(AnnotationContext ctx) {
        boolean isMethod = false;
        ParserRuleContext parent = ctx.getParent();
        while (parent.getParent() != null) {
            if (parent instanceof ClassBodyDeclarationContext) {
                ClassBodyDeclarationContext cBodyCtx = (ClassBodyDeclarationContext) parent;
                if (cBodyCtx.memberDeclaration() != null)
                    isMethod = true;
                break;
            }
            parent = parent.getParent();
        }
        return isMethod;
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