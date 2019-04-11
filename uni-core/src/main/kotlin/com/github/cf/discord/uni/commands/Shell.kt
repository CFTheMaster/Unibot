package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

@Load
@Argument("shell", "string")
class Shell : Command(){
    override val ownerOnly = true
    override val desc = "execute some shell commands"

    override fun run(ctx: Context) {
        val output = "${ctx.args["shell"]}".runCommand(File("../").absoluteFile).toString()
        if (output.toCharArray().size < 2000){
            ctx.send("```shell\n" + output + "```")
        }else{
            ctx.send("message to long has been send to console")
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

