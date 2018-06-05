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
            id = "change_my_color",
            aliases = ["rainbow"],
            description = "change your own color",
            usage = "#123456 | random | nothing to remove the color"
    )
    fun myColor(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if(author!!.isBot){
            return
        }else {
            val user = event.guild.getMember(event.author)
            val userRoleIds = event.guild.getMember(event.author).roles
            val serverRoles = event.guild.roles

            val roleId = serverRoles.find { it.name.startsWith("#${context.args!!.replaceFirst("#", "")}") }?.idLong

            val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
            val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
            val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()

            if(context.args == null){
                event.guild.controller.removeRolesFromMember(event.guild.getMember(event.author), serverRoles.find { it.name.startsWith("#", true) }).queue()
            }
            else if(context.args == "something"){
                val color = Color(randomColor, randomColor1, randomColor2)
                val colorName = java.lang.Integer.toHexString(color.rgb).replaceFirst("ff", "")
                event.guild.controller.removeRolesFromMember(event.guild.getMember(event.author), serverRoles.find { it.name.startsWith("#", true) }).queue()
                val newRole = event.guild.controller.createRole().setColor(color).setName("#$colorName").complete()
                event.guild.controller.addSingleRoleToMember(event.guild.getMember(event.author), newRole).queue()
            }else{
                if(roleId == null){
                    val ohShit = java.lang.Integer.parseInt(context.args!!.replaceFirst("#", ""), 16)
                    val hexAss = java.lang.Integer.toHexString(ohShit)
                    val color = Color(ohShit)
                    event.guild.controller.removeRolesFromMember(event.guild.getMember(event.author), serverRoles.find { it.name.startsWith("#", true) }).queue()
                    val haha = event.guild.controller.createRole().setColor(color).setName("#$hexAss").complete()
                    event.guild.controller.addSingleRoleToMember(event.guild.getMember(event.author), haha).queue()
                    event.channel.sendMessage("<@!${event.author.idLong}> changed your color!")
                }
                else if(userRoleIds.any({it.idLong == roleId})){
                    event.channel.sendMessage("<@!${event.author.idLong}>You already have this color!").queue()
                }
                else{
                    val role = event.guild.getRoleById(roleId)
                    event.guild.controller.addSingleRoleToMember(event.guild.getMember(event.author), role).queue()
                    event.channel.sendMessage("<@!${event.author.idLong}> changed your color!")
                }
            }
        }

    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "hex_to_int",
            aliases = ["hex"],
            description = "see what the int is for your current hex",
            usage = "#123456"
    )
    fun whatIsMyHexCommand(context: CommandContext, event: MessageReceivedEvent){
        val ohShit = java.lang.Integer.parseInt(context.args!!.replaceFirst("#", ""), 16)
        event.channel.sendMessage("your hex ${context.args} is $ohShit").queue()
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "int_to_hex",
            aliases = ["int"],
            description = "see what the current hex is for your int",
            usage = "12345678"
    )
    fun whatIsMyIntCommand(context: CommandContext, event: MessageReceivedEvent){
        val intName = java.lang.Integer.toHexString(context.args!!.toInt()).replaceFirst("ff", "")
        event.channel.sendMessage("your int: ${context.args} is #$intName").queue()
    }
}