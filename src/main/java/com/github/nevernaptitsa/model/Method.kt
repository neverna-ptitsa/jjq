package com.github.nevernaptitsa.model

internal data class Method(val name: String,
                           val visibility: Visibility,
                           val inputType: Type,
                           val outputType: Type)