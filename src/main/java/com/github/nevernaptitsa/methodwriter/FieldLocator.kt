package com.github.nevernaptitsa.methodwriter

import com.github.nevernaptitsa.model.Field
import com.github.nevernaptitsa.model.FieldAccessorInfo
import com.github.nevernaptitsa.model.Type

internal interface FieldLocator {
    fun locateFieldInfo(currentType: Type, fieldLogicalName: Field): FieldAccessorInfo?
}