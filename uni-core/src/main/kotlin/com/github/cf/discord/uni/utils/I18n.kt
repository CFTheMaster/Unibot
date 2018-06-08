package com.github.cf.discord.uni.utils

import java.util.*

object I18n {
    fun parse(str: String, values: Map<String, Any>): String {
        val regex = "\\{\\{([^}]+)}}".toRegex()
        var new = str

        while (regex.containsMatchIn(new)) {
            val match = regex.find(new)?.groupValues

            if (values.contains(match?.get(1)) && match != null) {
                new = new.replace(match[0], values[match[1]].toString())
            }
        }

        return new
    }

    fun permission(lang: ResourceBundle, perm: String): String = lang.getString(
            perm.split("_")[0].toLowerCase()
                    + perm.split("_").getOrElse(1, { "" }).toLowerCase().capitalize()
    )

    fun langToCode(lang: String): String = when (lang.toLowerCase()) {
        "english" -> "en_US"
        else -> "en_US"
    }
}