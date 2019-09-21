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

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context

@Load
@Argument("hex", "string")
class HexToInt : Command(){
    override val desc = "get the int value of a hex"

    override fun run(ctx: Context) {
        val hex = ctx.args["hex"] as String

        val ohShit = java.lang.Integer.parseInt(hex.replace("#", ""), 16)
        ctx.channel.sendMessage("your hex ${hex.toString()} is ${ohShit.toString()}").queue()
    }
}
