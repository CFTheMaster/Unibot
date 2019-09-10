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

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import java.time.Duration

@Load
class Ping : Command(){
    override val desc = "check the current ping of the bot"
    override val guildOnly = false

    override fun run(ctx: Context) {
        val receivedTime = ctx.msg.timeCreated.toInstant()
        ctx.channel.sendMessage("Gateway ping took: ${ctx.jda.gatewayPing}ms\n RestAPI ping took: ${ctx.jda.restPing}ms")
    }
}
