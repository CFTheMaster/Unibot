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
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.HttpUrl
import okhttp3.Request

@CommandGroup("query")
class GoogleCommand {

    companion object {
        val CUSTOM_SEARCH_URL_BUILDER: HttpUrl = HttpUrl.Builder()
                .scheme("https")
                .host("www.googleapis.com")
                .addPathSegments("customsearch/v1")
                .addQueryParameter("cx", EnvVars.GOOGLE_SEARCH_ENGINE)
                .addQueryParameter("key", EnvVars.GOOGLE_API_KEY)
                .build()
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "google",
            aliases = ["google", "g"],
            description = "Gives you a google link to click on.",
            usage = "<input to search on google>"
    )
    fun google(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if (author!!.isBot) {
            return
        } else {
            val query = context.args ?: return

            val url = CUSTOM_SEARCH_URL_BUILDER.newBuilder().addQueryParameter("q", query).build()
            val request = Request.Builder().url(url).build()
            val messageFuture = event.channel.sendMessage("Searching for **$query** on Google...").submit()

            // Query for google search results and edit message
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
                                    "${message.contentRaw}$LINE_SEPARATOR" +
                                            "**${titles.first()}** - ${results.first()["link"].asText()}$LINE_SEPARATOR" +
                                            "${results.first()["snippet"].asText()}$LINE_SEPARATOR" +
                                            LINE_SEPARATOR +
                                            "Other relevant results:$LINE_SEPARATOR" +
                                            if (titles.size > 1) titles.subList(1, titles.size).joinToString(LINE_SEPARATOR, prefix = "`", postfix = "`") else "none.").queue()
                        } else {
                            message.editMessage("${message.contentRaw} but no results were found.").queue()
                        }
                    }
        }
    }
}
