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
package com.github.cf.discord.uni.listeners

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.core.EnvVars
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.impl.JDAImpl
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class ReadyEventListener : ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {
        val text = arrayOf(
                "with computerfreaker \uD83C\uDF38",
                "with guns \uD83C\uDF38",
                "\uD83D\uDC9C computerfreaker",
                "is this thing on?",
                "doing nothing...")
        val idx = Random().nextInt(text.size)
        val random = text[idx]
        event.jda.presence.setPresence(OnlineStatus.ONLINE, Game.of(Game.GameType.STREAMING, "$random | ${EnvVars.PREFIX}help", "https://www.twitch.tv/computerfreaker"))
        updateStats(event.jda)
    }

    companion object {
        fun updateStats(jda: JDA) {
            val client = (jda as JDAImpl).httpClientBuilder.build()
            val body = JSONObject().put("server_count", jda.guilds.size)


            if (EnvVars.DBL_TOKEN?.isNotEmpty()!!) {
                client.newRequest({
                    post(RequestBody.create(MediaType.parse("application/json"), body.toString()))
                    url("https://discordbots.org/api/bots/${jda.selfUser.id}/stats")
                    header("Authorization", EnvVars.DBL_TOKEN)
                    header("Content-Type", "application/json")
                }).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) = response.close()

                    override fun onFailure(call: Call, e: IOException)
                    {
                        Uni.LOGGER.error("Failed to send information to discordbots.org", e)
                    }
                })
            }

            if (EnvVars.TERMINAL_TOKEN?.isNotEmpty()!!) {
                client.newRequest({
                    post(RequestBody.create(MediaType.parse("application/json"), body.toString()))
                    url("https://ls.terminal.ink/api/v1/bots/${jda.selfUser.id}")
                    header("Authorization", EnvVars.TERMINAL_TOKEN)
                    header("Content-Type", "application/json")
                }).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) = response.close()

                    override fun onFailure(call: Call, e: IOException)
                    {
                        Uni.LOGGER.error("Failed to send information to ls.terminal.ink", e)
                    }
                })
            }
        }
        private inline fun OkHttpClient.newRequest(lazy: Request.Builder.() -> Unit) : Call {
            val builder = Request.Builder()
            builder.lazy()
            return newCall(builder.build())
        }
    }
}
