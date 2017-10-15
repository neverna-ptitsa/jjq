package com.github.nevernaptitsa

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.PrintWriter

internal class JJQMethodWriter(private val output: PrintWriter,
                      private val expression: String,
                      private val method: Method,
                      private val fieldLocator: FieldLocator,
                      private val typeInspector: TypeInspector) : JJQBaseListener() {
    companion object {
        val TYPE_ITERABLE = Type("java.lang.Iterable")
    }

    interface FieldLocator {
        fun locateFieldInfo(currentType: Type, fieldLogicalName: Field): FieldAccessorInfo?
    }

    interface TypeInspector {
        fun subclasses(typeToCheck: Type, typeToSubclass: Type): Boolean
        fun collectionContents(typeToUnpack: Type): Type
    }

    class JJQParseException(message: String) : Exception(message)
    enum class AccessorType { METHOD, BARE }
    enum class Visibility {
        PUBLIC, PRIVATE, PACKAGE;

        override fun toString(): String {
            return super.toString().toLowerCase()
        }
    }
    data class FieldAccessorInfo(
            val accessorName: String,
            val resultType: Type,
            val accessorType: AccessorType = AccessorType.METHOD
    )
    data class Method(val name: String, val visibility: Visibility, val inputType: Type, val outputType: Type)
    data class Type(val name: String)
    data class Field(val name: String)

    var currentType: Type? = null
    var currentPadding = 0

    fun writeMethod() {
        val lexer = JJQLexer(CharStreams.fromString(expression))
        val tokens = CommonTokenStream(lexer)
        val parser = JJQParser(tokens)
        val expression = parser.jjqExpression()
        val walker = ParseTreeWalker()
        walker.walk(this, expression)
    }

    override fun enterJjqExpression(ctx: JJQParser.JjqExpressionContext?) {
        beginMethod()
        beginStreamBasedOnInputType()
    }

    override fun exitJjqExpression(ctx: JJQParser.JjqExpressionContext?) {
        endStreamBasedOnOutputType()
        endMethod()
    }

    private fun endStreamBasedOnOutputType() {
        pad()
        when (typeInspector.subclasses(method.outputType, Type("java.util.Collection"))) {
            true -> output.printf(".collect(java.util.stream.Collectors.toCollection(%s::new));\n",
                                    method.outputType.name)
            false -> output.printf(".findFirst().get();\n")
        }
        popPadding()
    }

    private fun beginStreamBasedOnInputType() {
        pad()
        when (typeInspector.subclasses(method.inputType, TYPE_ITERABLE)) {
            true -> {
                output.printf("return arg.stream()\n")
                currentType = typeInspector.collectionContents(method.inputType)
            }
            false -> {
                output.printf("return java.util.stream.Stream.of(arg)\n")
                currentType = method.inputType
            }
        }
        pushPadding()
    }

    override fun enterStageExpression(ctx: JJQParser.StageExpressionContext?) {
        val parseRuleContexts = ctx!!.getRuleContexts(ParserRuleContext::class.java)
        val lastParseRuleContext = parseRuleContexts.lastOrNull()
        when (lastParseRuleContext) {
            is JJQParser.ArrayFlatMapContext -> {
                pad()
                output.printf(".flatMap(v->v")
            }
            is JJQParser.StructureSelectorContext -> {
                pad()
                output.printf(".map(v->v")
            }
            else -> {
                pad()
                output.printf(".map(v->")
            }
        }
    }

    override fun enterStructureSelectorOrBooleanLiteral(ctx: JJQParser.StructureSelectorOrBooleanLiteralContext?) {
        val structureSelector = ctx?.structureSelector()
        val t = ctx?.TRUE()
        val f = ctx?.FALSE()
        if (structureSelector != null) {
            output.printf("v")
        }
        if (t != null) {
            output.printf("true")
        }
        if (f != null) {
            output.printf("false")
        }
    }

    override fun enterBooleanExpressionR(ctx: JJQParser.BooleanExpressionRContext?) {
        val and = ctx?.AND()
        val or = ctx?.OR()
        if (and != null) {
            output.printf(" && ")
        }
        if (or != null) {
            output.printf(" || ")
        }
    }

    override fun exitStageExpression(ctx: JJQParser.StageExpressionContext?) {
        output.printf(")\n")
    }

    override fun enterArrayFlatMap(ctx: JJQParser.ArrayFlatMapContext?) {
        output.printf(".stream()")
        currentType = typeInspector.collectionContents(currentType!!)
    }

    override fun enterStructureSelector(ctx: JJQParser.StructureSelectorContext?) {
        val fieldName = ctx?.ID()?.text
        val field = when (fieldName) {
            null -> return // an empty '.' means the value of the current node
            else -> Field(fieldName)
        }
        val accessorInfo = fieldLocator.locateFieldInfo(currentType!!, field)
        when (accessorInfo) {
            null -> {
                val symbol = ctx.ID().symbol
                val error = String.format("Unable to locate accessor for field %s of class %s at col %s line %s",
                        fieldName,
                        currentType!!.name,
                        symbol.charPositionInLine,
                        symbol.line)
                throw JJQParseException(error)
            }
            else -> {
                output.printf(".%s%s",
                    accessorInfo.accessorName,
                    when (accessorInfo.accessorType) {
                        AccessorType.METHOD -> "()"
                        AccessorType.BARE -> ""
                    }
                )
                currentType = accessorInfo.resultType
            }
        }

    }

    private fun pad() {
        for (i in 0..currentPadding) {
            output.print(' ')
        }
    }

    private fun beginMethod() {
        pad()
        output.printf("%s %s %s(%s arg){\n",
                method.visibility,
                method.outputType.name,
                method.name,
                method.inputType.name)
        pushPadding()
    }

    private fun pushPadding() {
        currentPadding += 2
    }

    private fun endMethod() {
        popPadding()
        pad()
        output.println("}\n")
    }

    private fun popPadding() {
        currentPadding -= 2
    }

}