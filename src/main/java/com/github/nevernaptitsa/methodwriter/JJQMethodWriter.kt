package com.github.nevernaptitsa.methodwriter

import com.github.nevernaptitsa.JJQParser
import com.github.nevernaptitsa.JJQLexer
import com.github.nevernaptitsa.model.Method
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.PrintWriter
import org.antlr.v4.runtime.tree.ParseTreeListener
import java.util.Arrays.asList

internal class JJQMethodWriter(private val fieldLocator: FieldLocator,
                               private val typeInspector: TypeInspector) {

    fun writeMethod(output: PrintWriter,
                    expression: String,
                    method: Method) {
        val lexer = JJQLexer(CharStreams.fromString(expression))
        val tokens = CommonTokenStream(lexer)
        val parser = JJQParser(tokens)
        val expression = parser.jjqExpression()
        val walker = ParseTreeWalker()
        val sharedState = WriterSharedState(output, method)
        val listener = ProxyParseTreeListener(asList(
                MethodStartEndListener(sharedState, typeInspector),
                StreamStageListener(sharedState),
                StructureSelectionListener(sharedState, fieldLocator),
                FlatMapListener(sharedState, typeInspector),
                BooleanListener(sharedState)
        ) as List<ParseTreeListener>)
        walker.walk(listener, expression)
    }

}
