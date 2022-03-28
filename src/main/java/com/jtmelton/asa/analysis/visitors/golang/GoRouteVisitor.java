package com.jtmelton.asa.analysis.visitors.golang;

import com.jtmelton.asa.analysis.generated.antlr4.golang.GoParser;
import com.jtmelton.asa.analysis.generated.antlr4.golang.GoParserBaseVisitor;
import com.jtmelton.asa.analysis.utils.GolangAstNodes;
import com.jtmelton.asa.analysis.visitors.IRouteVisitor;
import com.jtmelton.asa.analysis.visitors.Language;
import com.jtmelton.asa.analysis.visitors.Phase;
import com.jtmelton.asa.domain.Route;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GoRouteVisitor extends GoParserBaseVisitor<Void> implements IRouteVisitor {

    private final String HANDLE_FUNCTION = "Handle";

    private final Set<String> HTTP_FUNCTIONS = new HashSet<>();

    private final Collection<Route> routes = new ArrayList<>();

    public GoRouteVisitor() {
        HTTP_FUNCTIONS.add(HANDLE_FUNCTION);
        // TODO: Handle Get without erroneously picking up incorrect, unrelated functions
        //HTTP_FUNCTIONS.add("Get");
        HTTP_FUNCTIONS.add("Post");
        HTTP_FUNCTIONS.add("Put");
    }

    @Override
    public Void visitPrimaryExpr(GoParser.PrimaryExprContext ctx) {
        TerminalNode ident = ctx.IDENTIFIER();

        if(ident != null && HTTP_FUNCTIONS.contains(ident.toString())) {
            if(!(ctx.parent instanceof GoParser.PrimaryExprContext)) {
                return super.visitPrimaryExpr(ctx);
            }

            GoParser.PrimaryExprContext parent = (GoParser.PrimaryExprContext) ctx.parent;
            GoParser.ArgumentsContext args = parent.arguments();
            GoParser.ExpressionListContext expList = args.expressionList();

            if(expList.children.size() < 1) {
                return super.visitPrimaryExpr(ctx);
            }

            expList.children.get(0).accept(new GoParserBaseVisitor<Void>() {
                @Override
                public Void visitLiteral(GoParser.LiteralContext ctx) {
                    String route = ctx.basicLit().string_().INTERPRETED_STRING_LIT().toString();
                    route = route.replace("\"", "");

                    String fileName = GolangAstNodes.GetSourceFileName(ctx);

                    String method;
                    if (HANDLE_FUNCTION.equals(ident.toString())) {
                        method = HANDLE_FUNCTION;
                    } else {
                        method = ident.toString();
                    }

                    routes.add(new Route(fileName, method, route, new ArrayList<>()));

                    return super.visitLiteral(ctx);
                }
            });
        }

        return super.visitPrimaryExpr(ctx);
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
        return Language.GOLANG == lang;
    }
}
