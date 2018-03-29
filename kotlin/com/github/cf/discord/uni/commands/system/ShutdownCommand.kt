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
package com.github.cf.discord.uni.commands.system

import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

@CommandGroup("system")
class ShutdownCommand {

    @Command(
            prefix = "uni!",
            id = "shutdown",
            aliases = ["shutdown"],
            description = "Shuts down the bot."
    )
    @Permissions(
            reqBotOwner = true
    )
    fun onCommand(context: CommandContext, event: MessageReceivedEvent) {
        event.channel.sendMessage("**Shutting down in 3 seconds.**").queue({
            val timer = Timer()
            timer.schedule(timerTask { exitProcess(ReturnCodes.SHUTDOWN) }, 3000)
        })
    }
}
