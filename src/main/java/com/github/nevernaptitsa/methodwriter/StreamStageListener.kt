package com.github.nevernaptitsa.methodwriter

import com.github.nevernaptitsa.JJQBaseListener
import com.github.nevernaptitsa.JJQParser
import org.antlr.v4.runtime.ParserRuleContext

internal class StreamStageListener(private val sharedState: WriterSharedState) : JJQBaseListener() {
    override fun enterStageExpression(ctx: JJQParser.StageExpressionContext?) {
        val parseRuleContexts = ctx!!.getRuleContexts(ParserRuleContext::class.java)
        val lastParseRuleContext = parseRuleContexts.lastOrNull()
        when (lastParseRuleContext) {
            is JJQParser.ArrayFlatMapContext -> {
                sharedState.pad()
                sharedState.output.printf(".flatMap(v->v")
            }
            is JJQParser.StructureSelectorContext -> {
                sharedState.pad()
                sharedState.output.printf(".map(v->v")
            }
            else -> {
                sharedState.pad()
                sharedState.output.printf(".map(v->")
            }
        }
    }

    override fun exitStageExpression(ctx: JJQParser.StageExpressionContext?) {
        sharedState.output.printf(")\n")
    }
}