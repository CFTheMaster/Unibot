package com.github.cf.discord.uni.commands.userColors

import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.extensions.findRoles
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.jetbrains.kotlin.cfg.pseudocode.and
import java.awt.Color

@CommandGroup("MyColor")
class changeMyColor {
    companion object {
        private val pattern = Regex("#[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]").toPattern()
    }
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "color",
            aliases = ["rainbow"],
            description = "change your own color",
            usage = "#123456 | random | nothing to remove the color"
    )
    fun myColor(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if(author!!.isBot){
            return
        }else{
            val aNewColor = event.guild.roles
            if(context.args.isNullOrEmpty()){
                event.guild.controller.removeRolesFromMember(event.guild.getMember(event.author), aNewColor.find { it.name.startsWith("#", false) }).queue()
            }
            else if(context.args.equals("something")){
                val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
                val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
                val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
                val color = Color(randomColor, randomColor1, randomColor2)
                val colorName = java.lang.Integer.toHexString(color.rgb).replaceFirst("ff", "")
                event.guild.controller.removeRolesFromMember(event.guild.getMember(event.author), aNewColor.find { it.name.startsWith("#", false) }).queue()
                val haha = event.guild.controller.createRole().setColor(color).setName("#$colorName").complete()
                event.guild.controller.addSingleRoleToMember(event.guild.getMember(event.author), haha).queue()
            }
            else{
                val ohShit = java.lang.Integer.parseInt(context.args!!.replaceFirst("#", ""), 16)
                val hexAss = java.lang.Integer.toHexString(ohShit)
                val color = Color(ohShit)
                event.guild.controller.removeRolesFromMember(event.guild.getMember(event.author), aNewColor.find { it.name.startsWith("#", false) }).queue()
                val haha = event.guild.controller.createRole().setColor(color).setName("#$hexAss").complete()
                event.guild.controller.addSingleRoleToMember(event.guild.getMember(event.author), haha).queue()
            }
        }
    }
}