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

import com.github.cf.discord.uni.Lib
import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.http.HttpQuery
import okhttp3.HttpUrl
import okhttp3.Request

@Load
@Argument("query", "string")
@Alias("wiki")
class Wikipedia : Command(){
    override val desc = "Search on Wikipedia!"
    override val nsfw = true
    override val cate = Category.NSFW.title

    override fun run(ctx: Context) {
        val query = ctx.args["query"] as String
        val url = SEARCH_URL_BUILDER.newBuilder().addQueryParameter("srsearch", query).build()
        val request = Request.Builder().url(url).build()
        val messageFuture = ctx.channel.sendMessage("Searching for **$query** on Wikipedia...").submit()

        HttpQuery.queryMono(request)
                .doOnError { messageFuture.get().editMessage("Search for **$it** on Wikipedia failed! ${it.localizedMessage}").queue() }
                .flatMap(HttpQuery::responseBody)
                .map { HttpQuery.OBJECT_MAPPER.readTree(it.bytes())["query"]["search"].toList() }
                .doOnError { messageFuture.get().editMessage("Could not read response from Wikipedia!").queue() }
                .subscribe {
                    val message = messageFuture.get()
                    if (it.isNotEmpty()) {
                        val articleTitles = it.map { it["title"].asText() }
                        message.editMessage(
                                "${message.contentRaw}${Lib.LINE_SEPARATOR}" +
                                        "${getArticleUrl(articleTitles.first())}${Lib.LINE_SEPARATOR}" +
                                        "Other relevant articles: ${
                                        if (articleTitles.size > 1)
                                            articleTitles.subList(1, articleTitles.size).joinToString()
                                        else "none."}").queue()
                    } else {
                        message.editMessage("${message.contentRaw} but no results were found.").queue()
                    }
                }
    }

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

    private fun getArticleUrl(title: String): String {
        return "$ARTICLE_BASE_URL${title.replace(' ', '_')}"
    }
}
