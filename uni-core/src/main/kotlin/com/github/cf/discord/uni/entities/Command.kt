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
package com.github.cf.discord.uni.entities

import com.github.cf.discord.uni.commands.system.Category


abstract class Command {
    val subcommands = mutableMapOf<String, Command>()
    open val name = ""
    open val desc = ""
    open val ownerOnly = false
    open val noHelp = false
    open val guildOnly = false
    open val nsfw = false
    open val cooldown = 5
    open val cate = Category.GENERAL.name

    abstract fun run(ctx: Context)

    fun addSubcommand(cmd: Command, name: String? = null) {
        subcommands[(name ?: if (cmd.name.isEmpty()) cmd::class.simpleName ?: return else cmd.name).toLowerCase()] = cmd
    }
}
