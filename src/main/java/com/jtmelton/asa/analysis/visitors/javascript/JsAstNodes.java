package com.jtmelton.asa.analysis.visitors.javascript;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.jtmelton.asa.analysis.generated.antlr4.ecmascript6.JavaScriptParser.*;

public class JsAstNodes {
  public static Optional<SingleExpressionContext> findImmediateDispatcher(SingleExpressionContext ctx) {
    return findMemberDotExpression(ctx).map(MemberDotExpressionContext::singleExpression);
  }

  public static Optional<IdentifierNameContext> findMemberDotFunction(SingleExpressionContext ctx) {
    return findMemberDotExpression(ctx).map(MemberDotExpressionContext::identifierName);
  }

  public static Optional<MemberDotExpressionContext> findMemberDotExpression(SingleExpressionContext ctx) {
    MemberDotExpressionContext memberDotExp = null;

    if(ctx instanceof MemberDotExpressionContext) {
      memberDotExp = (MemberDotExpressionContext) ctx;
    } else if(ctx instanceof ArgumentsExpressionContext) {
      SingleExpressionContext node = ((ArgumentsExpressionContext)ctx).singleExpression();
      while(node instanceof ArgumentsExpressionContext) {
        node = ((ArgumentsExpressionContext) node).singleExpression();
      }

      if(node instanceof MemberDotExpressionContext) {
        memberDotExp = (MemberDotExpressionContext) node;
      }
    }

    return Optional.ofNullable(memberDotExp);
  }

  public static Optional<TerminalNode> getIdentifier(SingleExpressionContext ctx) {
    if(!(ctx instanceof IdentifierExpressionContext)) {
      return Optional.empty();
    }

    return Optional.of(((IdentifierExpressionContext) ctx).Identifier());
  }

  public static Optional<TerminalNode> findIdentifier(SingleExpressionContext ctx) {
    Optional<TerminalNode> identifier;

    Optional<MemberDotExpressionContext> memberDotExp = findMemberDotExpression(ctx);
    if(memberDotExp.isPresent()) {
      identifier = findIdentifier(memberDotExp.get().singleExpression());
    } else if(ctx instanceof ArgumentsExpressionContext) {
      ArgumentsExpressionContext argExpCtx = (ArgumentsExpressionContext) ctx;
      identifier = findIdentifier(argExpCtx.singleExpression());
    } else {
      identifier = getIdentifier(ctx);
    }

    return identifier;
  }

  public static Optional<TerminalNode> findTerminalNode(IdentifierNameContext ctx) {
    TerminalNode node = ctx.Identifier();

    //Parser bug! Erroneously thinks delete from app.delete
    // is a reserved keyword
    if(node == null) {
      node = ctx.reservedWord().keyword().Delete();
    }

    return Optional.ofNullable(node);
  }

  public static boolean isInvocationReturnInvoked(SingleExpressionContext ctx) {
    if(!(ctx instanceof ArgumentsExpressionContext)) {
      return false;
    }

    ArgumentsExpressionContext argsExp = (ArgumentsExpressionContext) ctx;
    if(!(argsExp.singleExpression() instanceof ArgumentsExpressionContext)) {
      return false;
    }

    return argsExp.arguments().singleExpression().size() == 0;
  }

  public static List<SingleExpressionContext> getInvocationArgs(SingleExpressionContext ctx) {
    if(!(ctx instanceof ArgumentsExpressionContext)) {
      return Collections.EMPTY_LIST;
    }

    return ((ArgumentsExpressionContext) ctx).arguments().singleExpression();
  }
}
