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
import kotlin.math.min

@CommandGroup("query")
class UrbanDictionaryCommand {

    companion object {
        val SEARCH_URL_BUILDER: HttpUrl = HttpUrl.Builder()
                .scheme("https")
                .host("api.urbandictionary.com")
                .addPathSegments("v0/define")
                .build()
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "urbandictionary",
            aliases = ["udict"],
            description = "Searches for definitions from Urban Dictionary.",
            usage = "<input to search on Urban Dictionary>"
    )
    fun urbanDictionary(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if (author!!.isBot) {
            return
        } else {
            if (event.textChannel.isNSFW) {
                val query = context.args ?: return
                val url = SEARCH_URL_BUILDER.newBuilder().addQueryParameter("term", query).build()
                val request = Request.Builder().url(url).build()
                val messageFuture = event.channel.sendMessage("Searching for **$query** on Urban Dictionary...").submit()

                HttpQuery.queryMono(request)
                        .doOnError { messageFuture.get().editMessage("Search for **$it** on Urban Dictionary failed! ${it.localizedMessage}").queue() }
                        .flatMap(HttpQuery::responseBody)
                        .map { HttpQuery.OBJECT_MAPPER.readTree(it.bytes())["list"].toList() }
                        .doOnError { messageFuture.get().editMessage("Could not read response from Urban Dictionary!").queue() }
                        .subscribe {
                            val message = messageFuture.get()
                            if (it.isNotEmpty()) {
                                message.editMessage(it.subList(0, min(5, it.size)).mapIndexed { i, node ->
                                    "`${i + 1}. ${node["definition"].textValue().let {
                                        if (it.length > 150) {
                                            "${it.substring(0, 147)}..."
                                        } else {
                                            it
                                        }
                                    }}`"
                                }.joinToString(separator = LINE_SEPARATOR)).queue()
                            } else {
                                message.editMessage("${message.contentRaw} but no results were found.").queue()
                            }
                        }
            }
            else{
                event.message.channel.sendMessage("this channel is not nsfw").queue()
            }
        }
    }
}
