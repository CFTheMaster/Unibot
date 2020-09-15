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

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.modifyIf
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import java.time.format.DateTimeFormatter
import java.util.Comparator
import kotlin.math.max


@Load
@Alias("og")
class OldestMember : Command(){
    override val desc = "Get the oldest members"
    override val guildOnly = true
    override val cate = Category.GENERAL.name

    fun run(ctx: Context) {
        val member = ctx.member

        val embed = EmbedBuilder().apply {
            setAuthor("Oldest Members", null, ctx.guild!!.iconUrl)

            member?.let {
                val joins = ctx.guild!!.memberCache.sortedWith(Comparator.comparing(Member::getTimeJoined))
                var index = joins.indexOf(member)
                appendDescription("**Join Date*:* ${member.timeJoined.format(DateTimeFormatter.ISO_LOCAL_DATE)} `[#${index + 1}]`")
                appendDescription("**Join Order:** ")
                index = max(index - 3, 0)
                joins[index].let {m -> appendDescription(m.user.name.modifyIf(member == m) { "**[$it]()**" })}
                for (i in index + 1 until index + 7){
                    if(i >= joins.size) break
                    val m = joins[i]
                    val name = m.user.name.modifyIf(member == m) { "**[$it]()**" }
                    appendDescription(" > $name")
                }
            }
        }

        ctx.send(embed.build())
    }


}
