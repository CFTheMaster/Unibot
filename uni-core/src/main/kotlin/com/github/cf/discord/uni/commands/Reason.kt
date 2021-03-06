/*
 *   Copyright (C) 2017-2021 computerfreaker
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

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Arguments
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.database.schema.ModLogs
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import net.dv8tion.jda.api.Permission
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

@Load
@Perm(Permission.MANAGE_SERVER)
@Arguments(
        Argument("case", "string"),
        Argument("reason", "string")
)
class Reason : Command() {
    override val guildOnly = true
    override val desc = "Give a reason for a case in modlogs."
    override val cate = Category.MODERATION.name

    override fun run(ctx: Context) {
        if (ctx.storedGuild!!.modlogChannel == null) {
            return ctx.send("no modlog channel has been found")
        }

        val reasonArg = ctx.args["reason"] as String

        if (reasonArg.length > 512) {
            return ctx.send("reason too long")
        }

        val caseArg = (ctx.args["case"] as String).toLowerCase()

        asyncTransaction(Uni.pool) {
            val cases = ModLogs.select { ModLogs.guildId.eq(ctx.guild!!.idLong) }
            val caseIds: List<Int> = when (caseArg) {
                "l" -> listOf(cases.count())
                else -> {
                    if (caseArg.toIntOrNull() == null) {
                        if (!caseArg.matches("\\d+\\.\\.\\d+".toRegex())) {
                            return@asyncTransaction
                        }

                        val first = caseArg.split("..")[0].toInt()
                        val second = caseArg.split("..")[1].toInt()

                        if (first > second) {
                            return@asyncTransaction ctx.send("case num error")
                        }

                        var list = listOf<Int>()

                        for (i in first..second) {
                            list += i
                        }

                        list
                    } else
                        listOf(caseArg.toInt())
                }
            }

            for (id in caseIds) {
                ModLogs.update({ ModLogs.guildId.eq(ctx.guild!!.idLong) and ModLogs.caseId.eq(id) }) { it[reason] = reasonArg }
                val log = cases.firstOrNull { it[ModLogs.caseId] == id }

                if (log != null) {
                    ctx.guild!!
                            .getTextChannelById(ctx.storedGuild.modlogChannel ?: return@asyncTransaction)!!
                            .retrieveMessageById(log[ModLogs.messageId])
                            .queue({
                                it.editMessage(
                                        it.contentRaw.replace(
                                                "Reason: .+\nResponsible moderator: .+".toRegex(),
                                                "Reason: [ ${ctx.author.name}#${ctx.author.discriminator} ] $reasonArg\n" +
                                                        "Responsible moderator: ${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id})"
                                        )
                                ).queue()
                            }) {
                                ctx.sendError(it)
                                it.printStackTrace()
                            }
                }
            }

            ctx.send("\uD83D\uDC4C")
        }.execute()
    }
}
