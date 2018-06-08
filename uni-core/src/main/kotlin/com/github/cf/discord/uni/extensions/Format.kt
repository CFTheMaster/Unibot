/*
 *   Copyright (C) 2017-2018 computerfreaker
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
package com.github.cf.discord.uni.extensions

import net.dv8tion.jda.core.entities.*
import java.time.OffsetDateTime

infix fun List<User>.multipleUsers(argument: String): String = listOut("user", argument) { it.formattedName(true) }
infix fun List<Member>.multipleMembers(argument: String): String = listOut("member", argument) { it.user.formattedName(true) }
infix fun List<TextChannel>.multipleTextChannels(argument: String): String = listOut("text channel", argument) { it.asMention }
infix fun List<VoiceChannel>.multipleVoiceChannels(argument: String): String = listOut("voice channel", argument) { it.name }
infix fun List<Role>.multipleRoles(argument: String): String = listOut("role", argument) { it.name }

inline fun <reified T> List<T>.listOut(kind: String, argument: String, conversion: (T) -> String): String = buildString {
    append("Multiple ${kind}s found matching \"$argument\":\n")
    val s = this@listOut.size
    for(i in 0 until 4) {
        append("${this@listOut[i].let(conversion)}\n")
        if(i == 3 && s > 4)
            append("And ${s - 4} other $kind${if(s - 4 > 1) "s..." else "..."}")
        if(i + 1 == s)
            break
    }
}

inline fun <reified U: User> U.formattedName(boldName: Boolean): String {
    return "${if(boldName) "**$name**" else name}#$discriminator"
}

inline val OffsetDateTime.readableFormat: String
    inline get() = "${dayOfWeek.niceName}, ${month.niceName} $dayOfMonth, $year"

inline val <reified T: Enum<T>> T.niceName: String
    inline get() = name.replace("_", " ").run { "${this[0]}${substring(1).toLowerCase()}" }

fun noMatch(lookedFor: String, query: String): String = "Could not find any $lookedFor matching \"$query\"!"

fun String.filterMassMention(): String = replace("@everyone", "@\u0435veryone").replace("@here", "@h\u0435re").trim()