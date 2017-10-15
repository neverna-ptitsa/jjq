package com.github.nevernaptitsa.model

internal enum class Visibility {
    PUBLIC, PRIVATE, PACKAGE;

    override fun toString(): String {
        return super.toString().toLowerCase()
    }
}