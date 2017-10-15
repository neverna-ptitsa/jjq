package com.github.nevernaptitsa.model

internal data class FieldAccessorInfo(
        val accessorName: String,
        val resultType: Type,
        val accessorType: AccessorType = AccessorType.METHOD
)