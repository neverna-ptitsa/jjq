package com.github.nevernaptitsa.methodwriter

import com.github.nevernaptitsa.JJQBaseListener
import com.github.nevernaptitsa.JJQParser
import com.github.nevernaptitsa.model.Type

internal class MethodStartEndListener(private val sharedState: WriterSharedState,
                                      private val typeInspector: TypeInspector) : JJQBaseListener() {

    override fun enterJjqExpression(ctx: JJQParser.JjqExpressionContext?) {
        beginMethod()
        beginStreamBasedOnInputType()
    }

    override fun exitJjqExpression(ctx: JJQParser.JjqExpressionContext?) {
        endStreamBasedOnOutputType()
        endMethod()
    }

    private fun endStreamBasedOnOutputType() {
        sharedState.pad()
        when (typeInspector.subclasses(sharedState.method.outputType, Type("java.util.Collection"))) {
            true -> sharedState.output.printf(".collect(java.util.stream.Collectors.toCollection(%s::new));\n",
                    sharedState.method.outputType.name)
            false -> sharedState.output.printf(".findFirst().get();\n")
        }
        sharedState.popPadding()
    }

    private fun beginStreamBasedOnInputType() {
        sharedState.pad()
        when (typeInspector.subclasses(sharedState.method.inputType, Type.TYPE_ITERABLE)) {
            true -> {
                sharedState.output.printf("return arg.stream()\n")
                sharedState.currentType = typeInspector.collectionContents(sharedState.method.inputType)
            }
            false -> {
                sharedState.output.printf("return java.util.stream.Stream.of(arg)\n")
                sharedState.currentType = sharedState.method.inputType
            }
        }
        sharedState.pushPadding()
    }

    private fun beginMethod() {
        sharedState.pad()
        sharedState.output.printf("%s %s %s(%s arg){\n",
                sharedState.method.visibility,
                sharedState.method.outputType.name,
                sharedState.method.name,
                sharedState.method.inputType.name)
        sharedState.pushPadding()
    }

    private fun endMethod() {
        sharedState.popPadding()
        sharedState.pad()
        sharedState.output.println("}\n")
    }
}