package com.github.nevernaptitsa.methodwriter

import com.github.nevernaptitsa.JJQBaseListener
import com.github.nevernaptitsa.JJQParser

internal class FlatMapListener(private val sharedState: WriterSharedState,
                               private val typeInspector: TypeInspector) : JJQBaseListener() {
    override fun enterArrayFlatMap(ctx: JJQParser.ArrayFlatMapContext?) {
        sharedState.output.printf(".stream()")
        sharedState.currentType = typeInspector.collectionContents(sharedState.currentType)
    }
}