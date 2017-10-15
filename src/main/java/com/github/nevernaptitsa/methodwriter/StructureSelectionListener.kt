package com.github.nevernaptitsa.methodwriter

import com.github.nevernaptitsa.JJQBaseListener
import com.github.nevernaptitsa.JJQParser
import com.github.nevernaptitsa.model.AccessorType
import com.github.nevernaptitsa.model.Field

internal class StructureSelectionListener(private val sharedState: WriterSharedState,
                                          private val fieldLocator: FieldLocator) : JJQBaseListener() {
    override fun enterStructureSelector(ctx: JJQParser.StructureSelectorContext?) {
        val fieldName = ctx?.ID()?.text
        val field = when (fieldName) {
            null -> return // an empty '.' means the value of the current node
            else -> Field(fieldName)
        }
        val accessorInfo = fieldLocator.locateFieldInfo(sharedState.currentType, field)
        when (accessorInfo) {
            null -> {
                val symbol = ctx.ID().symbol
                val error = String.format("Unable to locate accessor for field %s of class %s at col %s line %s",
                        fieldName,
                        sharedState.currentType.name,
                        symbol.charPositionInLine,
                        symbol.line)
                throw JJQParseException(error)
            }
            else -> {
                sharedState.output.printf(".%s%s",
                        accessorInfo.accessorName,
                        when (accessorInfo.accessorType) {
                            AccessorType.METHOD -> "()"
                            AccessorType.BARE -> ""
                        }
                )
                sharedState.currentType = accessorInfo.resultType
            }
        }

    }
}