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

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.commands.system.ReturnCodes
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

@Load
class Restart : Command(){
    override val desc = "restart the bot"
    override val ownerOnly = true
    override val cate = Category.OWNER.name

    override fun run(ctx: Context) {
        ctx.channel.sendMessage("**Shutting down and restarting after 3 seconds.**").queue({
            val timer = Timer()
            timer.schedule(timerTask { exitProcess(ReturnCodes.RESTART) }, 3000)
        })
    }
}
