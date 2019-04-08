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
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

@CommandGroup("owner")
class ShellCommand {

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "shell",
            aliases = ["shell", "execute"],
            description = "Execute shell commands",
            usage = "<input to execute a shell command from kotlin>"
    )
    @Permissions(
            allowDm = true
    )
    fun shell(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        val command = context.args

        if(author!!.isBot) return
        else if (event.message.author.id in botOwners.authors) {
            val output = "$command".runCommand(File("../").absoluteFile).toString()
            if (output.toCharArray().size < 2000){
                event.message.channel.sendMessage("```shell\n" + output + "```").queue()
            }else{
                event.message.channel.sendMessage("message to long has been send to console").queue()
                print(output)
            }
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

    fun String.runCommand(workingDir: File): String? {
        try {
            val parts = this.split("\\s".toRegex())
            val proc = ProcessBuilder(*parts.toTypedArray())
                    .directory(workingDir)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start()

            proc.waitFor(60, TimeUnit.MINUTES)
            return proc.inputStream.bufferedReader().readText()
        } catch(e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}