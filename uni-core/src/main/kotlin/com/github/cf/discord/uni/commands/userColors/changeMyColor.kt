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
            else if(aNewColor.isEmpty()){
                val ohShit = java.lang.Integer.parseInt("${context.args}", 16)
                val color = Color(ohShit)
                val haha = event.guild.controller.createRole().setColor(color).setName(ohShit.toString()).complete()
                event.guild.controller.removeRolesFromMember(event.guild.getMember(event.author), aNewColor.find { it.name.startsWith("#", false) }).queue()
                event.guild.controller.addSingleRoleToMember(event.guild.getMember(event.author), haha)
            }else if(context.args.equals("something")){
                event.guild.controller.removeRolesFromMember(event.guild.getMember(event.author), aNewColor.find { it.name.startsWith("#", false) }).queue()
                val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
                val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
                val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
                val color = Color(randomColor, randomColor1, randomColor2)
                val colorName = java.lang.Integer.toHexString(color.rgb)
                val haha = event.guild.controller.createRole().setColor(color).setName(colorName).complete()
                event.guild.controller.addSingleRoleToMember(event.guild.getMember(event.author), haha)
            }
            else{
                val existedRole = aNewColor.find { it.name.equals(context.args, true) }
                event.guild.controller.removeRolesFromMember(event.guild.getMember(event.author), aNewColor.find { it.name.startsWith("#", false) }).queue()
                event.guild.controller.addSingleRoleToMember(event.guild.getMember(event.author), existedRole!!).queue()
            }


        }
    }
}