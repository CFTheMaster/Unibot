/*
 *   Copyright (C) 2017-2020 computerfreaker
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.cf.discord.uni

import com.github.cf.discord.uni.Lib.LINE_SEPARATOR
import java.util.regex.Pattern

object Lib {
    @JvmStatic
    val QUOTE_PATTERN: Pattern = Pattern.compile(""""([^"]*)"""")
    @JvmStatic
    val CODE_BLOCK_PATTERN: Pattern = Pattern.compile("""```\R*kotlin\R((?:.*\R*)*)```""")
    @JvmStatic
    val LINE_SEPARATOR: String = System.getProperty("line.separator") ?: "\n"
}

object Reactions {
    @JvmStatic
    val NUMBERS = arrayOf(
            "0⃣", // 0
            "1⃣", // 1
            "2⃣", // 2
            "3⃣", // 3
            "4⃣", // 4
            "5⃣", // 5
            "6⃣", // 6
            "7⃣", // 7
            "8⃣", // 8
            "9⃣"  // 9
    )
    @JvmStatic
    val EMOJI_TO_INT: Map<String, Int> = mapOf(
            "0⃣" to 0, // 0
            "1⃣" to 1, // 1
            "2⃣" to 2, // 2
            "3⃣" to 3, // 3
            "4⃣" to 4, // 4
            "5⃣" to 5, // 5
            "6⃣" to 6, // 6
            "7⃣" to 7, // 7
            "8⃣" to 8, // 8
            "9⃣" to 9  // 9
    )
    const val REGIONAL_CROSSMARK = "\uD83C\uDDFD"
    const val REGIONAL_CHECKMARK = "☑️"
    const val CHECKMARK = "✅"
    const val CROSSMARK = "❌"
    const val LEFT_ARROW = "⬅"
    const val RIGHT_ARROW = "➡"
}

fun String.getFromQuotes(): String {
    val matcher = Lib.QUOTE_PATTERN.matcher(this)
    return if (matcher.find()) {
        matcher.group(1)
    } else {
        this
    }
}

fun String.getFromCodeBlock(): String {
    val matcher = Lib.CODE_BLOCK_PATTERN.matcher(this)
    return if (matcher.find()) {
        matcher.group(1)
    } else {
        this
    }
}

fun String.italicize(): String = "*$this*"

fun String.bold(): String = "**$this**"

fun String.markdownUrl(url: String): String = "[$this]($url)"

fun String.strikethrough(): String = "~~$this~~"

fun String.code(): String = "`$this`"

fun String.codeblock(language: String = ""): String = "```$language$LINE_SEPARATOR$this$LINE_SEPARATOR```"

fun String.splitByLines(): List<String> = this.split(Lib.LINE_SEPARATOR)

inline fun <T, R : Comparable<R>> Iterable<T>.maxByList(selector: (T) -> R, greaterThan: () -> R): List<T> {
    val iterator = iterator()
    if (!iterator.hasNext()) return emptyList()

    val list: MutableList<T> = mutableListOf()
    var maxElem = iterator.next()
    var maxValue = selector(maxElem)
    if (maxValue > greaterThan()) list.add(maxElem)

    while (iterator.hasNext()) {
        val e = iterator.next()
        val v = selector(e)
        when {
            v > maxValue && v > greaterThan() -> {
                maxElem = e
                maxValue = v
                list.clear()
                list.add(maxElem)
            }
            v == maxValue && v > greaterThan() -> {
                maxElem = e
                list.add(maxElem)
            }
        }
    }
    return list
}

fun fastCeil(numerator: Int, denominator: Int): Int {
    return if (numerator > 1) 1 + ((numerator - 1) / denominator) else 1
}
