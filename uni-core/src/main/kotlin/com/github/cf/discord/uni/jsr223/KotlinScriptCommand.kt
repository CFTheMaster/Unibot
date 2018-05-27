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
package com.github.cf.discord.uni.jsr223

import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import com.github.cf.discord.uni.Lib.LINE_SEPARATOR
import com.github.cf.discord.uni.Reactions
import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.data.botOwners
import com.github.cf.discord.uni.getFromCodeBlock
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

@CommandGroup("scripting")
class KotlinScriptCommand {

    private val engine: ScriptEngine = ScriptEngineManager().getEngineByExtension("kts")

    init {
        // Warmup engine
        LOGGER.debug { "Launching a JSR223 ScriptEngine daemon for Kotlin..." }
        engine.eval("true != false")
    }

    @Command(
            id = "kotlin_script",
            aliases = ["kts"],
            prefix = "${EnvVars.PREFIX}",
            usage = "<>",
            description = "Evaluates a Kotlin script on runtime."
    )
    @Permissions(allowDm = true)
    fun ktscript(context: CommandContext, event: MessageReceivedEvent) {
        if(!event.author.isBot){
            if (event.message.author.id in botOwners.authors) {
                val args = context.args?.getFromCodeBlock() ?: return
                engine.setBindings(engine.createBindings().apply {
                    put("event", event)
                }, ScriptContext.ENGINE_SCOPE)
                try {
                    val eval: Any? = engine.eval("""import net.dv8tion.jda.core.events.message.MessageReceivedEvent${LINE_SEPARATOR}val event = bindings["event"]!! as MessageReceivedEvent$LINE_SEPARATOR$args""")
                    event.channel.sendMessage(
                            EmbedBuilder()
                                    .setTitle("Kotlin Scripting Engine")
                                    .setDescription("Evaluating ```kotlin$LINE_SEPARATOR$args```")
                                    .addField("Result", eval?.toString() ?: "*`void return`*", false)
                                    .build()
                    ).queue()
                    event.message.addReaction(Reactions.CHECKMARK).queue()
                } catch (e: ScriptException) {
                    event.message.addReaction(Reactions.CROSSMARK).queue()
                    event.channel.sendMessage(
                            EmbedBuilder()
                                    .setTitle("Kotlin Scripting Engine")
                                    .setDescription("An unexpected errorEmbed occurred in evaluating the Kotlin script.")
                                    .addField(e.javaClass.simpleName, "```kotlin$LINE_SEPARATOR${e.message}```", false)
                                    .build()
                    ).queue()
                }
            }
            else{
                event.channel.sendMessage(
                        EmbedBuilder()
                                .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                                .setTitle("Please don't do this command")
                                .setDescription("doing this command makes me angry please don't do it again <:OhISee:397902772865073154>")
                                .build()
                ).queue()
            }
        }
    }
}
