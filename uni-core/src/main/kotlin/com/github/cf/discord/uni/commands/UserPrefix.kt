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

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.database.schema.Users
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import org.jetbrains.exposed.sql.update
import java.util.*

@Argument("prefix", "string")
class AddUserPrefix : Command(){
    override val desc = "add user prefix"

    override fun run(ctx: Context) {
        val prefix = (ctx.args["prefix"] as String).toLowerCase().replace("add ", "").toByteArray()

        asyncTransaction(Uni.pool) {
            val encode = Base64.getEncoder().encodeToString(prefix)
            val decoded = String(Base64.getDecoder().decode(encode))

            try {
                Users.update({
                    Users.id.eq(ctx.author.idLong)
                }) {
                    it[customPrefix] = encode
                }
                ctx.send("Your current prefix is $decoded \n${ctx.author.asMention}")
            } catch (e: Throwable) {
                ctx.sendError(e)
            }
        }.execute()
    }
}


@Load
class UserPrefix : Command(){
    override val desc = "Add, view or delete the user prefix"

    init {
        addSubcommand(AddUserPrefix(), "add")
    }

    override fun run(ctx: Context){
        val decoded = String(Base64.getDecoder().decode(ctx.storedUser.customPrefix))

        ctx.send("Your current prefix is ${
        if(ctx.storedUser.customPrefix.isNullOrEmpty()) "none"
        else decoded } \n${ctx.author.asMention}")
    }

}
