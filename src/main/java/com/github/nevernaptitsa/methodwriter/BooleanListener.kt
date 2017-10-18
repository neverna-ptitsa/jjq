package com.github.nevernaptitsa.methodwriter

import com.github.nevernaptitsa.JJQBaseListener
import com.github.nevernaptitsa.JJQParser

internal class BooleanListener(private val sharedState: WriterSharedState) : JJQBaseListener() {
    override fun enterStructureSelectorOrBooleanLiteral(ctx: JJQParser.StructureSelectorOrBooleanLiteralContext?) {
        val structureSelector = ctx?.structureSelector()
        val t = ctx?.BOOLEAN_LITERAL()?.text
        if (structureSelector != null) {
            sharedState.output.printf("v")
        }
        when (t) {
            "true" -> sharedState.output.printf("true")
            "false" -> sharedState.output.printf("false")
        }
    }

    override fun enterBooleanExpressionR(ctx: JJQParser.BooleanExpressionRContext?) {
        val op = ctx?.BOOLEAN_OP()?.text
        when (op) {
            "and" -> sharedState.output.printf(" && ")
            "or" -> sharedState.output.printf(" || ")
            "!=" -> sharedState.output.printf(" != ")
            "==" -> sharedState.output.printf(" == ")
        }
    }
}