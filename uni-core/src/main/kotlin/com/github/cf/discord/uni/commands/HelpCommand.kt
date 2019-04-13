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
package com.github.cf.discord.uni.commands

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
import com.github.cf.discord.uni.core.EnvVars
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.requests.RestAction
import java.awt.Color
import java.util.StringJoiner

@CommandGroup("system.help")
class HelpCommand {

    companion object {
        const val EMBED_TITLE = "Uni Help Page"
        const val WEBSITE_URL = "https://uni.computerfreaker.cf/"
        const val COMMANDS_PER_PAGE = 10
        const val VERSION_NUMBER = "0.9.1"
        @JvmStatic
        val EMBED_COLOUR = Color(125, 165, 222)
    }
}
