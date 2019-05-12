package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.*
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.entities.ThreadedCommand
import net.dv8tion.jda.core.EmbedBuilder

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