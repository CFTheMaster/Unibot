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
package com.github.cf.discord.uni.utils

import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.text.SimpleDateFormat
import java.util.*

class Logger(loggerName: String) {
    val ANSI_RESET = "\u001B[0m"
    val ANSI_BLACK = "\u001B[30m"
    val ANSI_RED = "\u001B[31m"
    val ANSI_GREEN = "\u001B[32m"
    val ANSI_YELLOW = "\u001B[33m"
    val ANSI_BLUE = "\u001B[34m"
    val ANSI_PURPLE = "\u001B[35m"
    val ANSI_CYAN = "\u001B[36m"
    val ANSI_WHITE = "\u001B[37m"

    val ANSI_BLACK_BACKGROUND = "\u001B[40m"
    val ANSI_RED_BACKGROUND = "\u001B[41m"
    val ANSI_GREEN_BACKGROUND = "\u001B[42m"
    val ANSI_YELLOW_BACKGROUND = "\u001B[43m"
    val ANSI_BLUE_BACKGROUND = "\u001B[44m"
    val ANSI_PURPLE_BACKGROUND = "\u001B[45m"
    val ANSI_CYAN_BACKGROUND = "\u001B[46m"
    val ANSI_WHITE_BACKGROUND = "\u001B[47m"

    private val name = "$ANSI_BLACK_BACKGROUND$ANSI_YELLOW[ $loggerName ]$ANSI_RESET"

    private val time
        get() = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())

    private val timeText
        get() = "$ANSI_CYAN_BACKGROUND$ANSI_BLACK[ $time ]$ANSI_RESET"

    fun command(event: MessageReceivedEvent)
            = println(
            "$name $timeText " +
                    (if (event.guild != null)
                        "$ANSI_PURPLE${event.guild.name} (${event.guild.id})$ANSI_RESET -> $ANSI_GREEN${event.channel.name} (${event.channel.id})$ANSI_RESET"
                    else
                        "${ANSI_GREEN}DM$ANSI_RESET") +
                    " -> $ANSI_BLUE${event.author.name}#${event.author.discriminator} (${event.author.id})$ANSI_RESET:" +
                    " $ANSI_WHITE${event.message.contentDisplay}$ANSI_RESET"
    )

    fun info(vararg args: String)
            = println("$name $timeText $ANSI_PURPLE_BACKGROUND$ANSI_WHITE INF $ANSI_RESET ${args.joinToString(" ")}")

    fun error(vararg args: String)
            = println("$name $timeText $ANSI_RED_BACKGROUND$ANSI_WHITE ERR $ANSI_RESET ${args.joinToString(" ")}")

    fun error(text: String, e: Throwable)
            = error("$text\n$e\n${e.stackTrace.joinToString("\n") {
        "\tat ${it.className}(${it.fileName ?: "Unknown Source"})"
    }}")

    fun warn(vararg args: String)
            = println("$name $timeText $ANSI_YELLOW_BACKGROUND$ANSI_BLACK WRN $ANSI_RESET ${args.joinToString(" ")}")

    fun debug(vararg args: String)
            = println("$name $timeText $ANSI_WHITE_BACKGROUND$ANSI_BLACK DBG $ANSI_RESET ${args.joinToString(" ")}")
}
