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
package com.github.cf.discord.uni.commands.query

import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.command.CommandContext
import com.github.cf.discord.uni.Lib.LINE_SEPARATOR
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.http.HttpQuery
import com.github.cf.discord.uni.http.HttpQuery.OBJECT_MAPPER
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.HttpUrl
import okhttp3.Request

@CommandGroup("query")
class WikipediaCommand {

    companion object {
        val SEARCH_URL_BUILDER: HttpUrl = HttpUrl.Builder()
                .scheme("https")
                .host("en.wikipedia.org")
                .addPathSegments("w/api.php")
                .addQueryParameter("action", "query")
                .addQueryParameter("list", "search")
                .addQueryParameter("format", "json")
                .build()
        const val ARTICLE_BASE_URL = "https://en.wikipedia.org/wiki/"
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "wikipedia",
            aliases = ["wikipedia", "wiki"],
            description = "Searches wikipedia for articles.",
            usage = "<input to search on wikipedia>"
    )
    fun wikipedia(context: CommandContext, event: MessageReceivedEvent) {
        val query = context.args ?: return
        val url = SEARCH_URL_BUILDER.newBuilder().addQueryParameter("srsearch", query).build()
        val request = Request.Builder().url(url).build()
        val messageFuture = event.textChannel.sendMessage("Searching for **$query** on Wikipedia...").submit()

        HttpQuery.queryMono(request)
                .doOnError { messageFuture.get().editMessage("Search for **$it** on Wikipedia failed! ${it.localizedMessage}").queue() }
                .flatMap(HttpQuery::responseBody)
                .map { OBJECT_MAPPER.readTree(it.bytes())["query"]["search"].toList() }
                .doOnError { messageFuture.get().editMessage("Could not read response from Wikipedia!").queue() }
                .subscribe {
                    val message = messageFuture.get()
                    if (it.isNotEmpty()) {
                        val articleTitles = it.map { it["title"].asText() }
                        message.editMessage(
                                "${message.contentRaw}$LINE_SEPARATOR" +
                                        "${getArticleUrl(articleTitles.first())}$LINE_SEPARATOR" +
                                        "Other relevant articles: ${
                                        if (articleTitles.size > 1)
                                            articleTitles.subList(1, articleTitles.size).joinToString()
                                        else "none."}").queue()
                    } else {
                        message.editMessage("${message.contentRaw} but no results were found.").queue()
                    }
                }
    }

    private fun getArticleUrl(title: String): String {
        return "$ARTICLE_BASE_URL${title.replace(' ', '_')}"
    }
}
