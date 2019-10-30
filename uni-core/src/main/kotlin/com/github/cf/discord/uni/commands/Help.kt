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

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.data.botOwners
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.listeners.EventListener
import net.dv8tion.jda.api.EmbedBuilder

@Load
@Alias("--help", "-h")
@Argument("command", "string", true)
class Help : Command(){
    override val desc = "Gives you help with a command"

    override fun run(ctx: Context) {
        if("command" in ctx.args){
            val cmd = ctx.args["command"] as String
            if (cmd !in EventListener.cmdHandler.commands){
                ctx.send("command has not been found")
            } else {
                ctx.send(
                        EmbedBuilder().apply {
                            setTitle("Info for the command you asked")
                            setColor(ctx.member?.colorRaw ?: 6684876)
                            setDescription(EventListener.cmdHandler.help(cmd))
                            setFooter("requested by ${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id})", null)
                        }.build())
            }
        } else {

            val text = "All Commands: \nFlags:\n\n\t-h, --help\nGet help on a command!\n\n"

            val embed = EmbedBuilder().apply {
                setTitle(text)
                setColor(ctx.member?.colorRaw ?: 6684876)
                Category.values().forEach { category ->
                    val builder = StringBuilder()
                    if (ctx.author.id !in botOwners.authors)
                    {
                        EventListener.cmdHandler.commands
                                .toSortedMap().entries.stream()
                                .filter { Command -> Command.value.cate == category.name && Command.value.cate !== Category.OWNER.name && !Command.value.ownerOnly}
                                .forEach { Command -> builder.append("**`${Command.key}`**, ").filterNot {  it.category.name == Category.OWNER.name } }
                    } else {
                        EventListener.cmdHandler.commands
                                .toSortedMap().entries.stream()
                                .filter { Command -> Command.value.cate == category.name }
                                .forEach { Command -> builder.append("**`${Command.key}`**, ") }
                    }


                    addField(
                    if (ctx.author.id !in botOwners.authors) {
                        category.title.filterNot { category.title == Category.OWNER.title }
                    }else {
                        category.title
                    }, builder.toString(), true)
                }
                setFooter("requested by ${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id})", ctx.author.avatarUrl)
            }


            ctx.send(embed.build())
        }
    }
}
