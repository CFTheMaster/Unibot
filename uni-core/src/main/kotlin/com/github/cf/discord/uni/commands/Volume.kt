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
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.music.MusicManager
import net.dv8tion.jda.api.Permission

@Load
@Perm(Permission.MANAGE_SERVER)
@Alias("vol")
@Argument("volume", "number", true)
class Volume : Command(){
    override val desc = "Change the volume of the music"
    override val guildOnly = true
    override val cate = Category.MUSIC.name

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild!!.id]
                ?: return ctx.send("not connected to the channel")
        if ("volume" in ctx.args) {
            val vol = ctx.args["volume"] as Int

            if (vol > 200) {
                return ctx.send("Volume can't be put above 200")
            }

            if (vol < 0) {
                return ctx.send("can't put the volume below 0")
            }

            manager.player.volume = vol
        }

        ctx.send("volume has been changed to ${(manager.player.volume)}")
    }
}
