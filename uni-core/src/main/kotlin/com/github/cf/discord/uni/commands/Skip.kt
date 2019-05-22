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
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.music.MusicManager
import net.dv8tion.jda.core.Permission

@Load
@Perm(Permission.MANAGE_SERVER, true)
class Skip : Command() {
    override val desc = "Skips the current song"
    override val guildOnly = true

    override fun run(ctx: Context) {
        if (!ctx.member!!.voiceState.inVoiceChannel()) {
            return ctx.send("you aren't in the voice channel")
        }

        val manager = MusicManager.musicManagers[ctx.guild!!.id]
                ?: return ctx.send("Not connected to a voice channel")

        if (manager.scheduler.queue.isEmpty()) {
            return ctx.send("There's nothing in the queue!")
        }

        if (ctx.perms["MANAGE_SERVER"] == true) {
            manager.scheduler.next()
            ctx.send("force skipped")
        } else {
            val members = manager.voiceChannel.members.filter { !it.user.isBot }

            if (members.size - 1 <= manager.voteSkip.size) {
                manager.scheduler.next()

                return ctx.send("voteskip was success")
            }

            if (manager.voteSkip.contains(ctx.author.id)) {
                return ctx.send("you already voted")
            }

            if (members.size - 1 <= manager.voteSkip.size + 1) {
                manager.scheduler.next()

                return ctx.send("voteskip was success")
            }

            manager.voteSkip.add(ctx.author.id)
            ctx.send("voteskip add votes: ${manager.voteSkip.size}, votes needed for skip ${members.size - 1}")

        }
    }
}
