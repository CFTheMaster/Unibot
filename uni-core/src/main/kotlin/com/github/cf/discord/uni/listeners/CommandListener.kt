/*
 *   Copyright (C) 2017-2018 computerfreaker
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
package com.github.cf.discord.uni.listeners

import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.utils.PermissionUtil
import net.dv8tion.jda.core.Permission

class CommandListener : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val author = event?.author
        if(author!!.isBot) {
            return
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val channel = event?.channel
        val guild = event?.guild
        val author = event?.author
        if(!PermissionUtil.checkPermission(channel, guild?.selfMember, Permission.MESSAGE_WRITE)
                || !PermissionUtil.checkPermission(channel, guild?.selfMember, Permission.MESSAGE_EMBED_LINKS)
                || author!!.isBot) {
            return
        }
    }
}
