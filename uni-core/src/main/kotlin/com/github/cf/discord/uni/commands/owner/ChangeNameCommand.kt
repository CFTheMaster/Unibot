/*
 *   Copyright (C) 2017-2018 computerfreaker
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.cf.discord.uni.commands.owner

import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.data.botOwners
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

@CommandGroup("owner")
class ChangeNameCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "changename",
            aliases = ["name", "changename"],
            description = "Change the current name of the bot",
            usage = "<input to change the bot name>"
    )
    @Permissions(
            allowDm = true
    )
    fun changeName(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if(author!!.isBot) return
        else if(event.message.author.id in botOwners.authors){
            event.jda.selfUser.manager.setName(context.args).queue()
        }
        else{
            event.channel.sendMessage(
                    EmbedBuilder()
                            .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                            .setTitle("Please don't do this command")
                            .setDescription("doing this command makes me angry please don't do it again <:OhISee:397902772865073154>")
                            .build()
            ).queue()
        }
    }
}