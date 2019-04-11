package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory

@Load
@Argument("code", "string")
class Eval : Command(){
    override val ownerOnly = true
    override val desc = "Evaluate code (KotlinScript)"

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