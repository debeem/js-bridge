package com.debeem.wallet.npm.js_bridge_npm

fun Any?.toJson(): String {
    return when (this) {
        is Int, is Float, is Double -> this.toString()
        is String -> "\"$this\""
        is Map<*, *> -> this.entries.joinToString(",", "{", "}") { entry ->
            "\"${entry.key}\":${entry.value.toJson()}"
        }

        is Array<*> -> this.joinToString(",", "[", "]") { it.toJson() }
        is List<*> -> this.joinToString(",", "[", "]") { it.toJson() }
        else -> "\"$this\""
    }
}