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
package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.*
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.entities.ThreadedCommand
import net.dv8tion.jda.api.EmbedBuilder

@Load
@Arguments(
        Argument("username", "string")
)

@Flags(
        Flag("taiko", 't', "View taiko stats"),
        Flag("mania", 'm', "View osu!mania stats"),
        Flag("catch", 'c', "View catch the beat stats")
)
class Osu : ThreadedCommand() {
    override val desc = "Get your osu stats"

    override fun threadedRun(ctx: Context) {
        val username = (ctx.args["username"] as String)
        val mode = when {
            ctx.flags.argMap.containsKey("taiko") || ctx.flags.argMap.containsKey("t") -> "1"
            ctx.flags.argMap.containsKey("catch") || ctx.flags.argMap.containsKey("c") -> "2"
            ctx.flags.argMap.containsKey("mania") || ctx.flags.argMap.containsKey("m") -> "3"
            else -> "0"
        }

        val lemmyOsuUrl = "http://lemmmy.pw/osusig/sig.php?colour=hex8866ee&uname=$username&mode=$mode&pp=1&removeavmargin&flagshadow&flagstroke&darkheader&darktriangles&opaqueavatar&avatarrounding=4&rankedscore&onlineindicator=undefined&xpbar&xpbarhex"

        ctx.send(EmbedBuilder().apply {
            setTitle("User Score For User: $username", null)
            setImage(lemmyOsuUrl)
            setColor(ctx.member?.color)
            setFooter("Image Provided By https://www.lemmmy.pw/osusig", null)
        }.build())
    }
}
