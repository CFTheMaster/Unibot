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
package com.github.cf.discord.uni.commands.stateful.polls

import net.dv8tion.jda.core.entities.Member

data class PollOption(
        val optionName: String,
        private val votes: MutableSet<Member> = mutableSetOf()
) {

    fun add(user: Member) = votes.add(user)

    fun retract(user: Member) = votes.remove(user)

    fun count(): Int = votes.size

    fun votes(): List<Member> = votes.toList()

    override fun toString(): String = optionName
}
