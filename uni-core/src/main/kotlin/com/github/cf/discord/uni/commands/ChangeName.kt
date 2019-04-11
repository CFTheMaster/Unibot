package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context

@Load
@Argument("name", "string")
class ChangeName : Command(){
    override val ownerOnly = true
    override val desc = "Change the name of the bot"

    override fun run(ctx: Context) {
        ctx.jda.selfUser.manager.setName(ctx.args["name"] as String).queue()
    }
}