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
package com.github.cf.discord.uni.commands.info

import com.github.cf.discord.uni.core.EnvVars
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color

@CommandGroup("system")
class InviteCommand {

    @Command(
            prefix = "${EnvVars.PREFIX}",
            aliases = ["inv", "invite"],
            id = "Invite",
            description = "get invite to the bot"
    )
    @Permissions(
            allowDm = true
    )
    fun invite(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if (author!!.isBot) {
            return
        } else {
            val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
            val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
            val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
            val embedColor = Color(randomColor, randomColor1, randomColor2)

            val embed = EmbedBuilder()
                    .setTitle("invite for bot/support server")
                    .setColor(embedColor)
                    .setDescription("Uni invite: [click me](https://discordapp.com/oauth2/authorize?client_id=${event.jda.selfUser.id}&scope=bot&permissions=2146958591)")
                    .addField("invite to my server ", "[server invite]( https://discord.gg/rMVju6a)", true)
                    .addField("invite to my API server", "[API server invite](https://discord.gg/gzWwtWG)", true)
                    .setFooter("requested by ${event.author.name}#${event.author.discriminator} (${event.author.id})", "${event.author.avatarUrl}")
                    .build()
            event.textChannel.sendMessage(embed).queue()
        }
    }
}