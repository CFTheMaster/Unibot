package com.github.cf.discord.uni.commands.owner

import com.github.cf.discord.uni.core.EnvVars
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

@CommandGroup("owner")
class

ReloadCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "reload",
            aliases = ["reload"],
            description = "reload the current commands",
            usage = "<execute to reload all the current commands my bot has>"
    )
    @Permissions(
            allowDm = true
    )
    fun onCommand(context: CommandContext, event: MessageReceivedEvent){
        event.channel.sendMessage("Reload Command!!!").queue()
        if(context.args == null) event.channel.sendMessage("can't reload if you don't give any arguments").queue()
        else {
            try {

            } catch (e: Exception) {
                event.channel.sendMessage("oh no couldn't reload the command").queue()
            }
        }
    }
}