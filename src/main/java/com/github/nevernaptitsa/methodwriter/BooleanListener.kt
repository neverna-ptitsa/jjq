package com.github.nevernaptitsa.methodwriter

import com.github.nevernaptitsa.JJQBaseListener
import com.github.nevernaptitsa.JJQParser

internal class BooleanListener(private val sharedState: WriterSharedState) : JJQBaseListener() {
    override fun enterStructureSelectorOrBooleanLiteral(ctx: JJQParser.StructureSelectorOrBooleanLiteralContext?) {
        val structureSelector = ctx?.structureSelector()
        val t = ctx?.TRUE()
        val f = ctx?.FALSE()
        if (structureSelector != null) {
            sharedState.output.printf("v")
        }
        if (t != null) {
            sharedState.output.printf("true")
        }
        if (f != null) {
            sharedState.output.printf("false")
        }
    }

    override fun enterBooleanExpressionR(ctx: JJQParser.BooleanExpressionRContext?) {
        val and = ctx?.AND()
        val or = ctx?.OR()
        if (and != null) {
            sharedState.output.printf(" && ")
        }
        if (or != null) {
            sharedState.output.printf(" || ")
        }
    }
}