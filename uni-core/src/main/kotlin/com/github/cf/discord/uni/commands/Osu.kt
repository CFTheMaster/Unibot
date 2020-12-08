/*
 *   Copyright (C) 2017-2021 computerfreaker
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

import com.github.cf.discord.uni.Uni
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

        val lemmyOsuUrl = "https://osu.computerfreaker.pw/sig.php?colour=hex8866ee&uname=$username&mode=$mode&pp=1&removeavmargin&flagshadow&flagstroke&darkheader&darktriangles&opaqueavatar&avatarrounding=4&rankedscore&onlineindicator=undefined&xpbar&xpbarhex"

        try {
            ctx.send(EmbedBuilder().apply {
                setTitle("User Score For User: $username", null)
                setImage(lemmyOsuUrl)
                setColor(ctx.member?.color)
                setFooter("Image Provided By https://osu.computerfreaker.pw/", null)
            }.build())
        } catch (e: Exception) {
            ctx.send(EmbedBuilder().apply {
                setTitle("Command has errored", null)
                setDescription("Please message: **computerfreaker#0015** and send a screenshot of this error: $e\n**go to the support server: [support server](https://discord.gg/DDRbw7W)**")
                setFooter("go to support server by doing ${Uni.prefixes.firstOrNull()}support or clicking the link")
            }.build())
        }


    }
}
