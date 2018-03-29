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
package com.github.cf.discord.uni.commands.`fun`

import com.github.cf.discord.uni.core.EnvVars
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.concurrent.ThreadLocalRandom

@CommandGroup("fun")
class EightBallCommand {

    companion object {
        private val lines = listOf(
                "It is certain",
                "It is decidedly so",
                "Without a doubt",
                "Yes, definitely",
                "You may rely on it",
                "As I see it, yes",
                "Most likely",
                "Outlook good",
                "Yes",
                "Signs point to yes",
                "Reply hazy try again",
                "Ask again later",
                "Better not tell you now",
                "Cannot predict now",
                "Concentrate and ask again",
                "Don't count on it",
                "My reply is no",
                "My sources say no",
                "Outlook not so good",
                "Very doubtful")
        const val EIGHTBALL_EMOJI = "\uD83C\uDFB1"
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "eightball",
            aliases = ["8ball"],
            description = "The magic eight-ball sees all.",
            usage = "<question to ask 8ball>"
    )
    fun onCommand(context: CommandContext, event: MessageReceivedEvent) {
        val args = context.args ?: return
        event.textChannel.sendMessage("**Question:** $args\n$EIGHTBALL_EMOJI**: ${randAnswer()}**").queue()
    }

    private fun randAnswer(): String {
        return lines[ThreadLocalRandom.current().nextInt(lines.size)]
    }
}
