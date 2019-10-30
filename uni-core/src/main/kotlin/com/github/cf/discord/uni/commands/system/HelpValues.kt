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
package com.github.cf.discord.uni.commands.system

import java.awt.Color
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class HelpValues {

    companion object {

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

        const val EMBED_TITLE = "Uni Help Page"
        const val WEBSITE_URL = "https://uni.computerfreaker.cf/"
        const val COMMANDS_PER_PAGE = 10
        @JvmStatic
        val VERSION_NUMBER = "git rev-parse --short HEAD".runCommand(File("../").absoluteFile).toString()
        @JvmStatic
        val EMBED_COLOUR = Color(125, 165, 222)
    }
}
