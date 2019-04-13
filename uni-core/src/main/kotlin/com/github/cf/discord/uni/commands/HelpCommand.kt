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
package com.github.cf.discord.uni.commands

import java.awt.Color

class HelpCommand {

    companion object {
        const val EMBED_TITLE = "Uni Help Page"
        const val WEBSITE_URL = "https://uni.computerfreaker.cf/"
        const val COMMANDS_PER_PAGE = 10
        const val VERSION_NUMBER = "0.9.1"
        @JvmStatic
        val EMBED_COLOUR = Color(125, 165, 222)
    }
}
