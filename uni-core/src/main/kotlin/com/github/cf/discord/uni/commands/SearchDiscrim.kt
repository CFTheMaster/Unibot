/*
 *   Copyright (C) 2017-2020 computerfreaker
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
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context


@Load
@Alias("discrim")
@Argument("discrim", "string", true)
class SearchDiscrim : Command(){
    override val desc = "Search for a discrim"

    override fun run(ctx: Context) {
        val discrim = ctx.args["discrim"] as? String ?: ctx.author.discriminator
        val users = ctx.jda.users.filter { it.discriminator == discrim }
        val list = users.subList(0, Math.min(5, users.size)).joinToString("\n") { "\t${it.name} (${it.idLong})" }

        ctx.send("these users have the desired discrim \n$list")
    }
}
