package com.github.nevernaptitsa.model

internal data class Type(val name: String) {
    companion object {
        val TYPE_ITERABLE = Type("java.lang.Iterable")
    }
}