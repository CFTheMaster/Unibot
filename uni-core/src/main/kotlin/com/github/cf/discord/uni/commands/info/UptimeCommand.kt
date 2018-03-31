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
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant

@CommandGroup("system")
class UptimeCommand {

    private val startTime = Instant.now()

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "uptime",
            aliases = ["uptime"],
            description = "Shows how long the bot has been running for."
    )
    @Permissions(
            allowDm = true
    )
    fun uptime(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            event.channel.sendMessage("System uptime: ${Duration.between(startTime, Instant.now()).formatDuration()}").queue()
        }
    }

    private fun Duration.formatDuration(): String {
        val seconds = Math.abs(this.seconds)
        return "**${seconds / 86400}**d **${(seconds % 86400) / 3600}**h **${(seconds % 3600) / 60}**min **${seconds % 60}**s"
    }
}
