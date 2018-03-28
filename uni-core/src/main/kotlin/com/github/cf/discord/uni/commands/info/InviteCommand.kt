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

import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.annotations.RegistryAware
import com.github.kvnxiao.discord.meirei.command.CommandContext
import com.github.kvnxiao.discord.meirei.command.CommandDefaults
import com.github.kvnxiao.discord.meirei.command.CommandProperties
import com.github.kvnxiao.discord.meirei.command.DiscordCommand
import com.github.kvnxiao.discord.meirei.command.database.CommandRegistryRead
import com.github.kvnxiao.discord.meirei.utility.SplitString.Companion.splitString
import com.github.cf.discord.uni.Lib.LINE_SEPARATOR
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.requests.RestAction
import java.awt.Color
import java.time.Duration
import java.time.Instant
import java.util.StringJoiner

@CommandGroup("system")
class InviteCommand {

    companion object {
        @JvmStatic
        val EMBED_COLOUR = Color(125, 165, 222)
    }

    @Command(
            prefix = "uni!",
            aliases = ["inv", "invite"],
            id = "Invite",
            description = "get invite to the bot"
    )
    @Permissions(
            allowDm = true
    )
    fun invite(context: CommandContext, event: MessageReceivedEvent) {
        val embed = EmbedBuilder()
                .setColor(EMBED_COLOUR)
                .setDescription("Uni invite: [click me](https://discordapp.com/oauth2/authorize?client_id=${event.jda.selfUser.id}&scope=bot&permissions=66186303)")
                .build()
        event.textChannel.sendMessage(embed).queue()
    }
}