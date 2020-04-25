/*
 *   Copyright (C) 2017-2021 computerfreaker
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

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import java.util.concurrent.ThreadLocalRandom

@Load
@Argument("question", "string")
@Alias("8ball")
class EightBall : Command(){
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

    override val desc = "Get an answer from the magic 8ball"
    override val guildOnly = false

    override fun run(ctx: Context) {
        ctx.send("**Question:** ${ctx.args["question"] as String}\n$EIGHTBALL_EMOJI**: ${randAnswer()}**")
    }

    private fun randAnswer(): String {
        return lines[ThreadLocalRandom.current().nextInt(lines.size)]
    }
}
