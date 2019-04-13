package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.listeners.EventListener
import net.dv8tion.jda.core.EmbedBuilder

@Load
@Alias("--help", "-h")
@Argument("command", "string", true)
class Help : Command(){
    override val desc = "Gives you help with a command"

    override fun run(ctx: Context) {
        if("command" in ctx.args){
            val cmd = ctx.args["command"] as String
            if (cmd !in EventListener.cmdHandler.commands){
                ctx.send("command has not been found")
            } else {
                ctx.send(EventListener.cmdHandler.help(cmd))
            }
        } else {
            val commands = EventListener.cmdHandler.commands
                    .filter { !it.value.ownerOnly }
                    .toSortedMap()
                    .map {
                        "\t${it.key}" + " ".repeat(20 - it.key.length) + it.value.desc
                    }

            val text = "Flags:\n\n\t-h, --help${" ".repeat(10)}Get help on a command!\n\nCommands:\n\n`${commands.joinToString("\n")}`"
            val partSize = 40
            val parts = mutableListOf<String>()
            val lines = text.split("\n")
            var part = ""

            for (line in lines) {
                if (part.split("\n").size >= partSize) {
                    parts.add(part)
                    part = ""
                }

                part += "$line\n"
            }

            if (part.isNotBlank() && part.split("\n").size < partSize) {
                parts.add(part)
            }

            for (partt in parts){
                ctx.send(EmbedBuilder().apply {
                    setTitle("all current commands")
                    setColor(6684876)
                    setDescription(partt)
                    setFooter("requested by ${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id})", null)
                }.build())
            }
        }
    }
}