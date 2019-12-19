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
package com.github.cf.discord.uni.extensions

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.Guild

inline fun <reified J: JDA> J.findUsers(query: String): List<User> = FinderUtil.findUsers(query, this)
inline fun <reified J: JDA> J.findTextChannels(query: String): List<TextChannel> = FinderUtil.findTextChannels(query, this)
inline fun <reified J: JDA> J.findVoiceChannels(query: String): List<VoiceChannel> = FinderUtil.findVoiceChannels(query, this)
inline fun <reified J: JDA> J.findCategories(query: String): List<Category> = FinderUtil.findCategories(query, this)
inline fun <reified J: JDA> J.findEmotes(query: String): List<Emote> = FinderUtil.findEmotes(query, this)
inline fun <reified G: Guild> G.findBannedUsers(query: String): List<User>? = FinderUtil.findBannedUsers(query, this)
inline fun <reified G: Guild> G.findMembers(query: String): List<Member> = FinderUtil.findMembers(query, this)
inline fun <reified G: Guild> G.findTextChannels(query: String): List<TextChannel> = FinderUtil.findTextChannels(query, this)
inline fun <reified G: Guild> G.findVoiceChannels(query: String): List<VoiceChannel> = FinderUtil.findVoiceChannels(query, this)
inline fun <reified G: Guild> G.findCategories(query: String): List<Category> = FinderUtil.findCategories(query, this)
inline fun <reified G: Guild> G.findEmotes(query: String): List<Emote> = FinderUtil.findEmotes(query, this)
inline fun <reified G: Guild> G.findRoles(query: String): List<Role> = FinderUtil.findRoles(query, this)
