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
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.music.MusicManager

@Alias("unpause", "resume")
class Pause : Command() {
    override val desc = "Pause the current song!"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id]
                ?: return ctx.send("not connected to a voice channel"
                )
        val state = manager.player.isPaused

        manager.player.isPaused = !state

        if (!state) {
            ctx.send("paused")
        } else {
            ctx.send("resumed")
        }
    }
}
