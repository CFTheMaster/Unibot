package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.*
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.entities.ThreadedCommand
import net.dv8tion.jda.core.EmbedBuilder

@Load
@Arguments(
        Argument("username", "string"),
        Argument("mode", "string", true)
)

@Flags(
        Flag("taiko", 't', "View taiko stats"),
        Flag("mania", 'm', "View osu!mania stats"),
        Flag("catch", 'c', "View catch the beat stats")
)
class Osu : ThreadedCommand() {
    override fun threadedRun(ctx: Context) {
        val username = (ctx.args["username"] as String)
        val mode = ctx.args["mode"] as String

        val lemmyOsuUrl = "http://lemmmy.pw/osusig/sig.php?colour=hex8866ee&uname=$username&mode=${
        if(mode == "taiko" || mode == "t") 1
        else if (mode == "catch" || mode == "c") 2
        else if (mode == "mania" || mode == "m") 3
        else 0}&pp=1&removeavmargin&flagshadow&flagstroke&darkheader&darktriangles&opaqueavatar&avatarrounding=4&rankedscore&onlineindicator=undefined&xpbar&xpbarhex"

        ctx.send(EmbedBuilder().apply {
            setTitle("User Score For User: $username", null)
            setImage(lemmyOsuUrl)
            setColor(ctx.member?.color)
            setFooter("Image Provided By https://www.lemmmy.pw/osusig", null)
        }.build())
    }
}