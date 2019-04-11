package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context

@Load
@Argument("int", "integer")
class IntToHex : Command(){
    override val desc = "get the hex of an int"

    override fun run(ctx: Context) {
        val ith = ctx.args["int"] as Int

        val intName = java.lang.Integer.toHexString(ith).replaceFirst("ff", "")
        ctx.channel.sendMessage("your int: $ith is #$intName").queue()
    }
}