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
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.music.MusicManager
import net.dv8tion.jda.api.Permission

@Load
@Perm(Permission.MANAGE_SERVER)
class Stop : Command() {
    override val desc = "Stop the music!"
    override val cate = Category.MUSIC.name
    override fun run(ctx: Context) {
        if (MusicManager.musicManagers[ctx.guild!!.id] == null) {
            return ctx.send("not connected to a voice channel")
        }

        MusicManager.leave(ctx.guild.id)

        ctx.send("music has stopped playing")
    }
}
