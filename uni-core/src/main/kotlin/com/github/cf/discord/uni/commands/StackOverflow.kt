package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.utils.Http
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.HttpUrl
import org.json.JSONObject

@Load
@Argument("query", "string")
class StackOverflow : Command() {
    override val desc = "Search for answers on StackOverflow"

    override fun run(ctx: Context) {
        val query = ctx.args["query"] as String

        Http.get(HttpUrl.Builder().apply {
            scheme("https")
            host("api.stackexchange.com")
            addPathSegments("2.2/search/advanced")
            addQueryParameter("order", "asc")
            addQueryParameter("sort", "relevance")
            addQueryParameter("site", "stackoverflow")
            addQueryParameter("q", query)
        }.build()).thenAccept { res ->
            val json = JSONObject(res.body()!!.string())

            val items = json.getJSONArray("items")

            if (items.count() == 0) {
                return@thenAccept ctx.send("no results  have been found")
            }

            val item = items.getJSONObject(Math.floor(Math.random() * items.count()).toInt())

            val embed = EmbedBuilder().apply {
                val owner = item.getJSONObject("owner")
                val tags = item.getJSONArray("tags")
                val answered = item.getBoolean("is_answered")

                setTitle(item.getString("title"), item.getString("link"))
                setAuthor(
                        owner.getString("display_name"),
                        owner.getString("link"),
                        owner.getString("profile_image")
                )
                setColor(if (answered) 0x4CAF50 else 0xF44336)

                descriptionBuilder.append("**Tags**: ${tags.joinToString { it.toString() }}\n")

                if (answered && item.has("accepted_answer_id")) {
                    descriptionBuilder.append("\n[Answer](https://stackoverflow.com/a/${item.getInt("accepted_answer_id")})")
                }
            }

            ctx.send(embed.build())

            res.close()
        }.thenApply {}.exceptionally {
            LOGGER.error("Error while trying to get posts from StackOverflow", it)
            ctx.sendError(it)
        }
    }
}