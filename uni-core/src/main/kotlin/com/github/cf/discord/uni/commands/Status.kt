package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.Uni.Companion.prefix
import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game

@Load
@Alias("presence")
@Argument("status", "string")
class Status : Command(){
    override val ownerOnly = true
    override val desc = "change the status of the bot"

    override fun run(ctx: Context) {
        Uni.shardManager.setGame(Game.of(Game.GameType.STREAMING, "${ctx.args["status"] as String} | ${prefix}help", "https://www.twitch.tv/computerfreaker"))
        ctx.send("Status has been changed to: " + ctx.args["status"] as String)
    }
}