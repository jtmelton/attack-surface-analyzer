package com.jtmelton.asa.analysis.visitors.javascript.express;

import com.jtmelton.asa.analysis.generated.antlr4.ecmascript6.JavaScriptParserBaseVisitor;
import com.jtmelton.asa.analysis.utils.JsAstNodes;
import com.jtmelton.asa.analysis.visitors.IRouteVisitor;
import com.jtmelton.asa.analysis.visitors.Language;
import com.jtmelton.asa.analysis.visitors.Phase;
import com.jtmelton.asa.domain.Parameter;
import com.jtmelton.asa.domain.Route;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jtmelton.asa.analysis.generated.antlr4.ecmascript6.JavaScriptParser.*;

import java.lang.Void;

public class ExpressRouteVisitor extends JavaScriptParserBaseVisitor<Void> implements IRouteVisitor {

  private final Collection<Route> routes = new ArrayList<>();

  private final Collection<String> requestTypes = new ArrayList<>();

  private final String ROUTE_PARAM_PATTERN = ":[A-Za-z0-9_]+";

  private final String ROUTER = "Router";

  private final String EXPRESS = "express";

  private final String REQUIRE = "require";

  private TerminalNode expressNode= null;

  private TerminalNode expressAppNode = null;

  private TerminalNode expressRouterNode = null;

  public ExpressRouteVisitor() {
    requestTypes.add("get");
    requestTypes.add("post");
    requestTypes.add("delete");
    requestTypes.add("put");
  }

  @Override
  public Void visitAssignmentExpression(AssignmentExpressionContext ctx) {
    List<SingleExpressionContext> expressions = ctx.singleExpression();
    if(expressions.size() != 2) {
      return visitChildren(ctx);
    }

    SingleExpressionContext lhs = expressions.get(0);
    SingleExpressionContext rhs = expressions.get(1);

    if(expressNode == null && isExpressRequire(rhs)) {
      if(JsAstNodes.isInvocationReturnInvoked(rhs)) {
        JsAstNodes.getIdentifier(lhs).ifPresent(n -> expressAppNode = n);
      } else if(isRouterInvoked(rhs)) {
        JsAstNodes.getIdentifier(lhs).ifPresent(n -> expressRouterNode = n);
      } else {
        JsAstNodes.getIdentifier(lhs).ifPresent(n -> expressNode = n);
      }
    } else if(expressAppNode == null && expressNode != null) {
      if(isMatchingInvocation(rhs, expressNode.getText())){
        JsAstNodes.getIdentifier(lhs).ifPresent(n -> expressAppNode = n);
      }
    }

    return visitChildren(ctx);
  }

  @Override
  public Void visitVariableDeclaration(VariableDeclarationContext ctx) {
    if(ctx.Identifier() == null || ctx.singleExpression() == null) {
      return visitChildren(ctx);
    }

    TerminalNode lhs = ctx.Identifier();
    SingleExpressionContext rhs = ctx.singleExpression();

    if(expressNode == null && isExpressRequire(rhs)) {
      if(JsAstNodes.isInvocationReturnInvoked(rhs)) {
        expressAppNode = lhs;
      } else if(isRouterInvoked(rhs)) {
        expressRouterNode = lhs;
      } else {
        expressNode = lhs;
      }
    }
    if(expressAppNode == null && expressNode != null
        && isMatchingInvocation(rhs, expressNode.getText())) {
      expressAppNode = lhs;
    }
    if(expressRouterNode == null && isRouterInvocation(rhs)) {
      expressRouterNode = lhs;
    }

    return visitChildren(ctx);
  }

  @Override
  public Void visitArgumentsExpression(ArgumentsExpressionContext ctx) {
    Optional<SingleExpressionContext> dispatcher = JsAstNodes.findImmediateDispatcher(ctx);
    Optional<IdentifierNameContext> function = JsAstNodes.findMemberDotFunction(ctx);

    if(!function.isPresent()) {
      return visitChildren(ctx);
    }

    Optional<TerminalNode> functionName = JsAstNodes.findTerminalNode(function.get());

    dispatcher.ifPresent(d -> {
      if(isMatchingDispatcher(d, expressAppNode)
              || isMatchingDispatcher(d, expressRouterNode)) {
        functionName.ifPresent(f ->
          processRoutes(f.getText(), ctx.arguments().singleExpression())
        );
      }
    });

    return visitChildren(ctx);
  }

  private boolean isExpressRequire(SingleExpressionContext ctx) {
    return isMatchingInvocation(ctx, REQUIRE) && isExpressLib(ctx);
  }

  private boolean isRouterInvoked(SingleExpressionContext ctx) {
    Optional<IdentifierNameContext> function = JsAstNodes.findMemberDotFunction(ctx);

    final boolean[] result = { false };
    function.ifPresent(f -> result[0] = ROUTER.equals(f.Identifier().getText()));
    return result[0];
  }

  private boolean isMatchingInvocation(SingleExpressionContext exp, String invocationName) {
    final boolean[] result = { false };

    Optional<TerminalNode> identifier = JsAstNodes.findIdentifier(exp);
    identifier.ifPresent(i -> result[0] = invocationName.equals(i.getText()));

    return result[0];
  }

  private boolean isRouterInvocation(SingleExpressionContext ctx) {
    Optional<SingleExpressionContext> dispatcher = JsAstNodes.findImmediateDispatcher(ctx);
    Optional<IdentifierNameContext> function = JsAstNodes.findMemberDotFunction(ctx);

    if (expressNode == null) {
      return false;
    }

    final boolean[] result = { false };
    dispatcher.ifPresent(d -> {
      if(isMatchingDispatcher(d, expressNode)) {
        function.ifPresent(f -> result[0] = ROUTER.equals(f.Identifier().getText()));
      }
    });

    return result[0];
  }

  private void processRoutes(String functionName, List<SingleExpressionContext> args) {
    String path = getPath(args);
    if(path.isEmpty()) {
      return;
    }

    String fileName = JsAstNodes.getSourceFileName(args.get(0));

    if("all".equals(functionName)) {
      for(String type : requestTypes) {
        routes.add(new Route(fileName, type, path, getRouteParams(path)));
      }
    }else if(requestTypes.contains(functionName)) {
      routes.add(new Route(fileName, functionName, path, getRouteParams(path)));
    }
  }

  private boolean isExpressLib(SingleExpressionContext ctx) {
    boolean result = false;

    if(JsAstNodes.isInvocationReturnInvoked(ctx)) {
      result = isExpressLib(((ArgumentsExpressionContext) ctx).singleExpression());
    }

    List<SingleExpressionContext> args = JsAstNodes.getInvocationArgs(ctx);
    if(args.size() == 1 && args.get(0) instanceof LiteralExpressionContext) {
      LiteralExpressionContext literalExpCtx = (LiteralExpressionContext) args.get(0);
      TerminalNode literal = literalExpCtx.literal().StringLiteral();

      String module = cleanLiteral(literal);
      if(EXPRESS.equals(module)) {
        result = true;
      }
    } else {
      Optional<MemberDotExpressionContext> memberDotExp = JsAstNodes.findMemberDotExpression(ctx);
      if (memberDotExp.isPresent()) {
        result = isExpressLib(memberDotExp.get().singleExpression());
      }
    }
    return result;
  }

  private boolean isMatchingDispatcher(SingleExpressionContext dispatcher, TerminalNode terminalNode) {
    if (!(dispatcher instanceof IdentifierExpressionContext) || terminalNode == null) {
      return false;
    }

    TerminalNode base = ((IdentifierExpressionContext) dispatcher).Identifier();
    String baseName = base.getText();
    String expressAppNodeName = terminalNode.getSymbol().getText();

    return baseName.equals(expressAppNodeName);
  }

  private String getPath(List<SingleExpressionContext> args) {
    String path = "";

    if(args.size() > 0 && args.get(0) instanceof LiteralExpressionContext) {
      LiteralContext arg1 = ((LiteralExpressionContext) args.get(0)).literal();
      path = arg1.StringLiteral().getSymbol().getText();
    }

    return path;
  }

  private Collection<Parameter> getRouteParams(String path) {
    Collection<Parameter> params = new ArrayList<>();

    Pattern pattern = Pattern.compile(ROUTE_PARAM_PATTERN);
    Matcher matcher = pattern.matcher(path);
    while(matcher.find()) {
      String param = matcher.group();
      String cleanParam = param.replace(":", "");
      params.add(new Parameter("PathParam", cleanParam, ""));
    }

    return params;
  }

  private String cleanLiteral(TerminalNode node) {
    if(node == null || node.getSymbol() == null) {
      return "";
    }

    String text = node.getSymbol().getText();
    String cleanLiteral = text.replace("\"", "");
    cleanLiteral = cleanLiteral.replace("'", "");

    return cleanLiteral;
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
    return Language.JAVASCRIPT == lang;
  }
}
