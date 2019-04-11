package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.Lib
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.http.HttpQuery
import okhttp3.HttpUrl
import okhttp3.Request

@Load
@Argument("query", "string")
class Google : Command(){
    companion object {
        val CUSTOM_SEARCH_URL_BUILDER: HttpUrl = HttpUrl.Builder()
                .scheme("https")
                .host("www.googleapis.com")
                .addPathSegments("customsearch/v1")
                .addQueryParameter("cx", EnvVars.GOOGLE_SEARCH_ENGINE)
                .addQueryParameter("key", EnvVars.GOOGLE_API_KEY)
                .build()
    }

    override val desc = "search something on google"
    override val nsfw = true
    override val guildOnly = true

    override fun run(ctx: Context) {
        val query = ctx.args["query"] as String

        val url = CUSTOM_SEARCH_URL_BUILDER.newBuilder().addQueryParameter("q", query).build()
        val request = Request.Builder().url(url).build()
        val messageFuture = ctx.channel.sendMessage("Searching for **$query** on Google...").submit()

        HttpQuery.queryMono(request)
                .doOnError { messageFuture.get().editMessage("Search for **$it** on Google failed! ${it.localizedMessage}").queue() }
                .flatMap(HttpQuery::responseBody)
                .map { HttpQuery.OBJECT_MAPPER.readTree(it.bytes()) }
                .filter { it.has("items") }
                .map { it["items"].toList() }
                .subscribe { results ->
                    val message = messageFuture.get()
                    val titles = results.map { it["title"].asText() }
                    if (results.isNotEmpty()) {
                        message.editMessage(
                                "${message.contentRaw}${Lib.LINE_SEPARATOR}" +
                                        "**${titles.first()}** - ${results.first()["link"].asText()}${Lib.LINE_SEPARATOR}" +
                                        "${results.first()["snippet"].asText()}${Lib.LINE_SEPARATOR}" +
                                        Lib.LINE_SEPARATOR +
                                        "Other relevant results:${Lib.LINE_SEPARATOR}" +
                                        if (titles.size > 1) titles.subList(1, titles.size).joinToString(Lib.LINE_SEPARATOR, prefix = "`", postfix = "`") else "none.").queue()
                    } else {
                        message.editMessage("${message.contentRaw} but no results were found.").queue()
                    }
                }



    }
}