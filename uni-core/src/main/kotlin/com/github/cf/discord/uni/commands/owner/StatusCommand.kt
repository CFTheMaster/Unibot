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
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import com.github.cf.discord.uni.data.authorOnly
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import net.dv8tion.jda.core.EmbedBuilder

@CommandGroup("owner")
class StatusCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "status",
            aliases = ["status", "s"],
            description = "Change the current status",
            usage = "<input to change the playing status>"
    )
    @Permissions(
            allowDm = true
    )
    fun status(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if(author!!.isBot) return
        else if (event.message.author.id in authorOnly.authors) {
            event.jda.presence.setPresence(OnlineStatus.ONLINE, Game.of(Game.GameType.STREAMING, "${context.args} | ${EnvVars.PREFIX}help", "https://www.twitch.tv/computerfreaker"))
        }else{
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