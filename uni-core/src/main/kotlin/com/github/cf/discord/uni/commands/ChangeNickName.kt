package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context

@Load
@Argument("nickname", "string")
class ChangeNickName : Command(){
    override val ownerOnly = true
    override val desc = "Change the nickname of the bot"

    override fun run(ctx: Context) {
        ctx.guild!!.controller.setNickname(ctx.selfMember, ctx.args["nickname"] as String)
    }
}