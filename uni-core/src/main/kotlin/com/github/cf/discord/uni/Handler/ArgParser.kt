/*
 *   Copyright (C) 2017-2019 computerfreaker
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
package com.github.cf.discord.uni.Handler

object ArgParser {
    fun tokenize(str: String): List<String> {
        val tokenList = mutableListOf<String>()
        val characterIterator = str.iterator()
        var intermediateStringBuilder = StringBuilder()
        var eatRest = false
        var blockEatRest = false
        var eatingSingleQuotePair = false
        while (characterIterator.hasNext()) {
            val character = characterIterator.nextChar()
            // TODO clean up logic
            if (eatRest && ((!eatingSingleQuotePair && character == '\'') || (eatingSingleQuotePair && character == '"'))) {
                // that super rare case where another eatable quote-paired token follows after an unmatched quote
                intermediateStringBuilder.insert(0, if (character == '\'') '"' else '\'')
                intermediateStringBuilder.toString().trim().split(" ").forEach {
                    tokenList.add(it)
                }
                intermediateStringBuilder = StringBuilder()
                eatingSingleQuotePair = character == '\''
                continue
            } else if (!blockEatRest && eatRest && ((eatingSingleQuotePair && character == '\'') || (!eatingSingleQuotePair && character == '"'))) {
                eatRest = false
                eatingSingleQuotePair = false
                tokenList.add(intermediateStringBuilder.toString())
                intermediateStringBuilder = StringBuilder()
                continue
            } else if (characterIterator.hasNext() && !blockEatRest && (character == '"' || character == '\'')) {
                eatRest = true
                eatingSingleQuotePair = character == '\''
                continue
            } else if (!eatRest && character == ' ' && intermediateStringBuilder.isNotEmpty()) {
                tokenList.add(intermediateStringBuilder.toString())
                intermediateStringBuilder = StringBuilder()
                continue
            } else if (!blockEatRest && character == '\\') {
                blockEatRest = true
                continue
            } else if (!eatRest && character == ' ') continue
            // FIXME \\"foo bar" transforms into [\foo bar]
            blockEatRest = false
            intermediateStringBuilder.append(character)
        }
        if (eatRest) {
            // Unmatched quote at the beginning of the last token
            intermediateStringBuilder.insert(0, if (eatingSingleQuotePair) '\'' else '"')
            intermediateStringBuilder.toString().split(" ").forEach {
                tokenList.add(it)
            }
        } else if (intermediateStringBuilder.isNotEmpty()) {
            tokenList.add(intermediateStringBuilder.toString())
        }
        return tokenList
    }

    private fun parseKeyValuePair(pair: String, delimiter: Char = '='): Pair<String, String> {
        val keyBuilder = StringBuilder()
        val valueBuilder = StringBuilder()
        val kvIterator = pair.iterator()
        var eatingValue = false
        while (kvIterator.hasNext()) {
            val character = kvIterator.nextChar()
            if (!eatingValue && character == delimiter) {
                eatingValue = true
                continue
            }
            if (eatingValue) {
                valueBuilder.append(character)
            } else {
                keyBuilder.append(character)
            }
        }
        return keyBuilder.toString() to valueBuilder.toString()
    }

    fun untypedParseSplit(tokenList: List<String>): ParsedResult {
        val unmatched = mutableListOf<String>()
        val argMap = mutableMapOf<String, String?>()
        val tokenIterator = tokenList.iterator()
        var nextAsValueOf: String? = null
        while (tokenIterator.hasNext()) {
            val token = tokenIterator.next()
            if (token.startsWith('-')) {
                if (nextAsValueOf != null) {
                    argMap[nextAsValueOf] = null
                }
                if (token.contains('=')) {
                    val cut = if (token.startsWith("--")) 2 else 1
                    val keyValue = token.removeRange(0, cut)
                    if (keyValue.endsWith('=')) {
                        nextAsValueOf = keyValue.removeRange(keyValue.length-1, keyValue.length)
                    } else {
                        val keyValuePair = parseKeyValuePair(keyValue)
                        argMap[keyValuePair.first] = keyValuePair.second
                        nextAsValueOf = null
                    }
                    continue
                }
                if (token.startsWith("--")) {
                    nextAsValueOf = token.removeRange(0, 2) // TODO replace with drop
                } else {
                    val shorthandKeys = token.toCharArray().map { it.toString() }.toMutableList() // TODO maybe there's a map alternative
                    val lastShorthandValueKey = shorthandKeys.removeAt(shorthandKeys.lastIndex)
                    nextAsValueOf = lastShorthandValueKey
                    shorthandKeys.forEach {
                        argMap[it] = null
                    }
                }
                continue
            } else if (nextAsValueOf != null) {
                argMap[nextAsValueOf] = token
                nextAsValueOf = null
                continue
            }
            unmatched.add(token)
        }
        if (nextAsValueOf != null) {
            argMap[nextAsValueOf] = null
        }
        return ParsedResult(unmatched, argMap)
    }

    data class ParsedResult(val unmatched: List<String>, val argMap: Map<String, String?>)
}
