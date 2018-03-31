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
import com.github.kvnxiao.discord.meirei.utility.SplitString.Companion.splitString
import com.github.cf.discord.uni.Lib.LINE_SEPARATOR
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.getFromQuotes
import com.github.cf.discord.uni.http.HttpError
import com.github.cf.discord.uni.http.HttpQuery
import com.github.cf.discord.uni.query.pcpp.PcppBuildType
import com.github.cf.discord.uni.query.pcpp.PcppPartsList
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import reactor.core.publisher.Mono
import java.awt.Color
import java.net.URL
import java.util.StringJoiner

@CommandGroup("query.pcpartpicker")
class PcPartPickerCommand {

    companion object {
        // Table data indices for saved parts table
        const val TYPE_INDEX = 0
        const val IMAGE_INDEX = 1
        const val NAME_INDEX = 2
        const val PRICE_INDEX = 7
        const val PURCHASED_INDEX = 8
        const val BASE_URL = "https://ca.pcpartpicker.com"

        @JvmStatic
        val EMBED_COLOUR = Color(255, 218, 69)

        @JvmStatic
        private fun userCompletedBuildsUrl(user: String): URL = URL("$BASE_URL/user/$user/builds/")

        @JvmStatic
        private fun userSavedBuildsUrl(user: String, href: String = ""): URL = URL("$BASE_URL/user/$user/saved/${if (href.isNotEmpty()) href.substring(6) else href}")

        @JvmStatic
        private fun completedBuildUrl(href: String): URL = URL("$BASE_URL/b/${href.substring(6)}")
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "completedBuilds",
            aliases = ["pcpp"],
            description = "Searches PCPartPicker for completed builds by username",
            usage = "<PCPP username> <build name, can be in quotes>"
    )
    fun pcppCompleted(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            val args = context.args ?: return
            val (user, buildName) = splitString(args)
            user.let { userName ->
                val messageFuture = event.channel.sendMessage("Searching for **$userName**'s completed builds on PCPP.").submit()

                HttpQuery
                        .queryMono(Request.Builder().url(userCompletedBuildsUrl(userName)).build())
                        .doOnError { if (it is HttpError) userNotExistMsg(userName, messageFuture.get()) }
                        .flatMap(HttpQuery::responseBody)
                        .map { getHref(it.string(), buildName, PcppBuildType.COMPLETED) }
                        .doOnError { if (it !is HttpError) userNoBuildsMsg(userName, messageFuture.get()) }
                        .flatMap(this::getCompleted)
                        .doOnSuccess { sendBuildParts(it, messageFuture.get()) }
                        .subscribe()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "savedBuilds",
            parentId = "completedBuilds",
            aliases = ["s", "saved"],
            description = "Searches PCPartPicker for saved builds by username",
            usage = "<PCPP username> <build name, can be in quotes>"
    )
    fun pcppSaved(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            val args = context.args ?: return
            val (user, buildName) = splitString(args)
            user.let { userName ->
                val messageFuture = event.channel.sendMessage("Searching for **$userName**'s saved builds on PCPP.").submit()

                HttpQuery
                        .queryMono(Request.Builder().url(userSavedBuildsUrl(userName)).build())
                        .doOnError { if (it is HttpError) userNotExistMsg(userName, messageFuture.get()) }
                        .flatMap(HttpQuery::responseBody)
                        .map { getHref(it.string(), buildName, PcppBuildType.SAVED) }
                        .doOnError { if (it !is HttpError) userNoBuildsMsg(userName, messageFuture.get()) }
                        .flatMap { getSaved(userName, it) }
                        .doOnSuccess { sendBuildParts(it, messageFuture.get()) }
                        .subscribe()
            }
        }
    }

    private fun sendBuildParts(partsList: PcppPartsList, message: Message) {
        val embedBuilder = EmbedBuilder()
                .setColor(EMBED_COLOUR)
                .setAuthor("${partsList.username} - ${if (partsList.buildType == PcppBuildType.SAVED) "Saved Builds" else "Completed Builds"}", getUserUrl(partsList), getIconUrl(partsList.iconUrl))
                .addField("Build name: ${partsList.title}", getBuildUrl(partsList), false)
        var leftIndex = 0
        while (leftIndex < partsList.types.size) {
            var rightIndex = leftIndex
            while ((rightIndex + 1 < partsList.types.size) && partsList.types[rightIndex + 1] == partsList.types[rightIndex]) {
                rightIndex++
            }
            if (leftIndex == rightIndex) {
                embedBuilder.addField(partsList.types[leftIndex], "\u2022 ${partsList.names[leftIndex]}", false)
            } else {
                val joiner = StringJoiner("$LINE_SEPARATOR\u2022 ", "\u2022 ", "")
                for (i in leftIndex..rightIndex) {
                    joiner.add(partsList.names[i])
                }
                embedBuilder.addField(partsList.types[leftIndex], joiner.toString(), false)
            }
            leftIndex = rightIndex
            leftIndex++
        }
        message.editMessage(embedBuilder.build()).queue()
    }

    private fun getUserUrl(partsList: PcppPartsList): String {
        return when (partsList.buildType) {
            PcppBuildType.COMPLETED -> userCompletedBuildsUrl(partsList.username).toString()
            PcppBuildType.SAVED -> userSavedBuildsUrl(partsList.username).toString()
        }
    }

    private fun getBuildUrl(partsList: PcppPartsList): String {
        return when (partsList.buildType) {
            PcppBuildType.COMPLETED -> completedBuildUrl(partsList.href).toString()
            PcppBuildType.SAVED -> userSavedBuildsUrl(partsList.username, partsList.href).toString()
        }
    }

    private fun getIconUrl(url: String): String? {
        return when {
            url.startsWith("//cdn") -> "https:" + url
            url.startsWith("/static") -> BASE_URL + url
            else -> null
        }
    }

    private fun userNotExistMsg(user: String, message: Message) {
        message.editMessage("Couldn't retrieve info for user $user. Does this user exist on PCPP?").queue()
    }

    private fun userNoBuildsMsg(user: String, message: Message) {
        message.editMessage("The user $user did not have any builds available to show.").queue()
    }

    private fun getHref(bodyString: String, buildName: String?, buildType: PcppBuildType): String {
        val list: Elements = Jsoup.parse(bodyString)
                .getElementById(when (buildType) {
                    PcppBuildType.COMPLETED -> "userbuild_toc"
                    PcppBuildType.SAVED -> "saveobj_toc"
                })
                .select("li a")
        if (buildName != null) {
            val anchor = list.firstOrNull { it.text().contains(buildName.getFromQuotes(), true) }
            if (anchor != null) {
                return anchor.attr("href")
            }
        }
        return list.first().attr("href")
    }

    private fun getCompleted(href: String): Mono<PcppPartsList> = HttpQuery
            .queryMono(Request.Builder().url(completedBuildUrl(href)).build())
            .flatMap(HttpQuery::responseBody)
            .map { it.string() }
            .map { Jsoup.parse(it) }
            .map { getCompletedPartsList(it, href) }

    private fun getSaved(user: String, href: String = ""): Mono<PcppPartsList> = HttpQuery
            .queryMono(Request.Builder().url(userSavedBuildsUrl(user, href)).build())
            .flatMap(HttpQuery::responseBody)
            .map { it.string() }
            .map { Jsoup.parse(it) }
            .map { getSavedPartsList(it, href) }

    private fun getCompletedPartsList(document: Document, href: String): PcppPartsList {
        val titleBlock = document.getElementsByClass("title")
        val imgUrl = titleBlock.select("img").attr("src")
        val title = titleBlock.select("h1").text()
        val username = titleBlock.select(".owner a").text()
        val htmlBlock = document.getElementsByClass("parts").select("li")

        val typeList: MutableList<String> = mutableListOf()
        val nameList: MutableList<String> = mutableListOf()

        htmlBlock.forEach {
            val type = it.getElementsByClass("label").text()
            val name = it.getElementsByClass("name").text()
            if (type.isNotEmpty() && name.isNotEmpty()) {
                typeList.add(type)
                nameList.add(name)
            }
        }

        return PcppPartsList(href, title, username, typeList, nameList, imgUrl, PcppBuildType.COMPLETED)
    }

    private fun getSavedPartsList(document: Document, href: String): PcppPartsList {
        val titleBlock = document.getElementsByClass("avatar-username")
        val imgUrl = titleBlock.select("img").attr("src")
        val title = titleBlock.select("h1").text()
        val username = titleBlock.select("span").text()
        val tableBlock = document.getElementsByClass("manual-zebra").select("tbody tr")

        val elementList = tableBlock
                .map { it.children() }
                .filter { it.size == 10 }
                .toList()

        val typeList: MutableList<String> = mutableListOf()
        val nameList: MutableList<String> = mutableListOf()
        elementList.forEach {
            val type = it[TYPE_INDEX].text()
            val name = it[NAME_INDEX].text()
            typeList.add(if (type.isEmpty()) typeList.last() else type)
            nameList.add(name)
        }

        return PcppPartsList(href, title, username, typeList, nameList, imgUrl, PcppBuildType.SAVED)
    }
}
