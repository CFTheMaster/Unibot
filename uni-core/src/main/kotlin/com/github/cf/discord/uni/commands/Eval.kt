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

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory

@Load
@Argument("code", "string")
class Eval : Command(){
    override val ownerOnly = true
    override val desc = "Evaluate code (KotlinScript)"
    override val cate = Category.OWNER.name

    override fun run(ctx: Context) {
        val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

        engine.put("ctx", ctx)

        try {
            val res = engine.eval(
                    "import com.github.cf.discord.uni.Uni\n" +
                            "import com.github.cf.discord.uni.listeners.EventListener\n" +
                            "val ctx = bindings[\"ctx\"] as com.github.cf.discord.uni.entities.Context\n" +
                            ctx.rawArgs.joinToString(" "))
            ctx.sendCode("kotlin", res ?: "null")
        } catch (e: Throwable){
            ctx.sendCode("diff", "- $e")
        }
    }
}
