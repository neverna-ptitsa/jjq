package com.github.nevernaptitsa.methodwriter

import com.github.nevernaptitsa.model.Type

internal interface TypeInspector {
    fun subclasses(typeToCheck: Type, typeToSubclass: Type): Boolean
    fun collectionContents(typeToUnpack: Type): Type
}