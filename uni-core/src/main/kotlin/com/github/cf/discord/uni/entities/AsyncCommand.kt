package com.github.cf.discord.uni.entities

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.Uni.Companion.LOGGER
import kotlinx.coroutines.async

abstract class AsyncCommand : Command() {
    abstract suspend fun asyncRun(ctx: Context)

    override fun run(ctx: Context) {
        async(Uni.coroutineDispatcher) {
            try {
                asyncRun(ctx)
            } catch (e: Throwable) {
                LOGGER.error("Error while trying to execute asynchronous command", e)
                ctx.sendError(e)
            }
        }
    }
}