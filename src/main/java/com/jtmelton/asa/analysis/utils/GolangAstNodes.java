package com.jtmelton.asa.analysis.utils;

import com.jtmelton.asa.analysis.generated.antlr4.golang.GoParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;

public class GolangAstNodes {

    public static GoParser.SourceFileContext GetSourceFileCtx(RuleContext node) {
        RuleContext parent = node;

        while(parent != null && !(parent instanceof GoParser.SourceFileContext)) {
            parent = parent.getParent();
        }

        return (GoParser.SourceFileContext) parent;
    }

    public static String GetSourceFileName(ParserRuleContext ctx) {
        GoParser.SourceFileContext sourceCtx = GetSourceFileCtx(ctx);
        return sourceCtx.EOF().getSymbol().getTokenSource().getSourceName();
    }
}
