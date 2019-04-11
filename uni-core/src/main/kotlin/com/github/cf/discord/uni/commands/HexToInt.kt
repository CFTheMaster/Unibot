package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context

@Load
@Argument("hex", "string")
class HexToInt : Command(){
    override val desc = "get the int value of a hex"

    override fun run(ctx: Context) {
        val hex = ctx.args["hex"] as String

        val ohShit = java.lang.Integer.parseInt(hex.replace("#", ""), 16)
        ctx.channel.sendMessage("your hex $hex is $ohShit").queue()
    }
}