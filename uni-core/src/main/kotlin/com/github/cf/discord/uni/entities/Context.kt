package com.github.cf.discord.uni.entities

import com.github.cf.discord.uni.db.*
import com.github.cf.discord.uni.utils.ArgParser
import com.github.cf.discord.uni.utils.I18n
import mu.KotlinLogging
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.io.InputStream
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.jvm.jvmName

class   Context(
        val event: MessageReceivedEvent,
        val cmd: Command,
        val args: MutableMap<String, Any>,
        val rawArgs: List<String>,
        val flags: ArgParser.ParsedResult,
        val perms: MutableMap<String, Boolean>,
        val lang: ResourceBundle,
        val storedUser: DBUser,
        val storedGuild: DBGuild?
) {
    val logger = KotlinLogging.logger(cmd::class.jvmName)

    val jda: JDA = event.jda
    val guild: Guild? = event.guild
    val author: User = event.author
    val channel: MessageChannel = event.channel
    val msg: Message = event.message
    val member: Member? = event.member
    val selfMember: Member? = event.guild?.selfMember

    fun send(arg: String) = event.channel.sendMessage(arg).queue()
    fun send(arg: MessageEmbed) = event.channel.sendMessage(arg).queue()

    fun sendCode(lang: String, arg: Any) = event.channel.sendMessage("```$lang\n$arg```").queue()

    fun sendError(e: Throwable) = event.channel.sendMessage(
            I18n.parse(
                    lang.getString("error"),
                    mapOf("error" to e)
            )
    ).queue()

    fun getLastImage(): CompletableFuture<InputStream?> {
        val fut = CompletableFuture<InputStream?>()

        channel.history.retrievePast(25).queue({
            val history = it.filter { it.attachments.isNotEmpty() && it.attachments[0].isImage }

            if (history.isEmpty()) {
                fut.complete(null)
            } else {
                fut.complete(history[0].attachments[0].inputStream)
            }
        }) {
            fut.complete(null)
        }

        return fut
    }
}