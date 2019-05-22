/*
 *   Copyright (C) 2017-2019 computerfreaker
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

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.system.ReturnCodes
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

@Load
class UpdateBot : Command(){
    override val desc = "update the bot"
    override val ownerOnly = true

    override fun run(ctx: Context) {
        val output = "git pull".runCommand(File("../").absoluteFile).toString()
        if (output.toCharArray().size < 2000){
            ctx.channel.sendMessage("```shell\n" + output + "```\n" + "**Shutting down and restarting after 3 seconds.**").queue({
                val timer = Timer()
                timer.schedule(timerTask { exitProcess(ReturnCodes.RESTART) }, 3000)
            })

        }else {
            ctx.send("Message to long has been send to console")
            print(output)
        }
    }

    private fun String.runCommand(workingDir: File): String? {
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
            return e.toString()
        }
    }
}
