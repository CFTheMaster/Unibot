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

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.Uni.Companion.prefix
import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity

@Load
@Alias("presence")
@Argument("status", "string")
class Status : Command(){
    override val ownerOnly = true
    override val desc = "change the status of the bot"

    override fun run(ctx: Context) {
        Uni.shardManager.setActivity(Activity.streaming("${ctx.args["status"] as String} | ${prefix.firstOrNull()}help", "https://www.twitch.tv/computerfreaker"))
        ctx.send("Status has been changed to: " + ctx.args["status"] as String)
    }
}
