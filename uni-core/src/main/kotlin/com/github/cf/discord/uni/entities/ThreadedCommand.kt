package com.github.cf.discord.uni.entities

import com.github.cf.discord.uni.Uni


abstract class ThreadedCommand : Command() {
    abstract fun threadedRun(ctx: Context)

    override fun run(ctx: Context) = Uni.pool.execute { threadedRun(ctx) }
}