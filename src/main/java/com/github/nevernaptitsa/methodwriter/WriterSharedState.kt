package com.github.nevernaptitsa.methodwriter

import com.github.nevernaptitsa.model.Method
import com.github.nevernaptitsa.model.Type
import java.io.PrintWriter

internal class WriterSharedState(val output: PrintWriter,
                                 val method: Method) {
    var currentType: Type = Type("uninitialized")
    private var currentPadding = 0

    fun pad() {
        for (i in 0..currentPadding) {
            output.print(' ')
        }
    }

    fun pushPadding() {
        currentPadding += 2
    }

    fun popPadding() {
        currentPadding -= 2
    }

}