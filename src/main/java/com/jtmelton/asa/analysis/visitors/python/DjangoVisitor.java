package com.jtmelton.asa.analysis.visitors.python;

import com.jtmelton.asa.analysis.RouteAnalyzer;
import com.jtmelton.asa.analysis.generated.antlr4.python.PythonParser;
import com.jtmelton.asa.analysis.generated.antlr4.python.PythonParserBaseVisitor;
import com.jtmelton.asa.analysis.utils.PythonAstNodes;
import com.jtmelton.asa.analysis.visitors.IRouteVisitor;
import com.jtmelton.asa.domain.Route;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class DjangoVisitor extends PythonParserBaseVisitor<Void> implements IRouteVisitor {

    private final Collection<Route> routes = new ArrayList<>();

    private final Collection<String> urlRouteMethods = new ArrayList<>();

    public DjangoVisitor() {
        urlRouteMethods.add("path");
    }

    @Override
    public Void visitExpr(PythonParser.ExprContext ctx) {

            // 2-part expressions, in this case method-call and params
            if(ctx.getChildCount() == 2) {
                String path = null;

                for(int i = 0; i < ctx.getChildCount(); i++) {
                    ParseTree firstChild = ctx.getChild(0);

                    if(urlRouteMethods.contains(firstChild.getText())) {

                        // verify that this call to path() is wrapped in a urlpatterns array AND that it's in urls.py
                        EnclosureVerificationVisitor enclosureVerificationVisitor = new EnclosureVerificationVisitor();
                        PythonParser.StmtContext stmtContext = PythonAstNodes.getStatementContext(ctx);
                        stmtContext.getPayload().accept(enclosureVerificationVisitor);
                        boolean wrappingValid = enclosureVerificationVisitor.isWrappingValid();

                        if(wrappingValid) {
                            RouteParameterVisitor parameterVisitor = new RouteParameterVisitor();
                            ctx.getPayload().accept(parameterVisitor);
                            path = parameterVisitor.getName();
                        }
                    }
                }

                if(path != null) {
                    routes.add(new Route(PythonAstNodes.getSourceFileName(ctx), "Unknown", path, new HashSet<>()));
                }

            }
        return visitChildren(ctx);
    }

    @Override
    public void setPhase(Phase phase) {

    }

    @Override
    public Collection<Route> getRoutes() {
        return routes;
    }

    @Override
    public boolean acceptedPhase(Phase phase) {
        return Phase.ONE == phase;
    }

    @Override
    public boolean acceptedLang(RouteAnalyzer.Language lang) {
        return RouteAnalyzer.Language.PYTHON == lang;
    }

}
