package com.github.cf.discord.uni.listeners

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.core.EnvVars
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.impl.JDAImpl
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class GuildJoinLeaveListener : ListenerAdapter() {
    override fun onGuildJoin(event: GuildJoinEvent) {
        Uni.LOGGER.info("New guild: ${event.guild.name} (${event.guild.id})")

        updateStats(event.jda)
    }


    override fun onGuildLeave(event: GuildLeaveEvent) {
        Uni.LOGGER.info("Left guild: ${event.guild.name} (${event.guild.id}")

        updateStats(event.jda)
    }

    companion object {
        fun updateStats(jda: JDA) {
            val client = (jda as JDAImpl).httpClientBuilder.build()
            val body = JSONObject().put("server_count", jda.guilds.size)

            if (EnvVars.DBL_TOKEN.isNotEmpty()) {
                client.newRequest({
                    post(RequestBody.create(MediaType.parse("application/json"), body.toString()))
                    url("https://discordbots.org/api/bots/${jda.selfUser.id}/stats")
                    header("Authorization", EnvVars.DBL_TOKEN)
                    header("Content-Type", "application/json")
                }).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) = response.close()

                    override fun onFailure(call: Call, e: IOException)
                    {
                        Uni.LOGGER.error("Failed to send information to bots.discord.pw", e)
                    }
                })
            }

            if (EnvVars.TERMINAL_TOKEN.isNotEmpty()) {
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