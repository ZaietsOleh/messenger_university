package com.universitylab.plugins.socket

internal enum class IncomingTypes(val value: String) {
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe");

    companion object {
        fun fromString(value: String?): IncomingTypes? {
            return values().firstOrNull { it.value == value }
        }
    }
}