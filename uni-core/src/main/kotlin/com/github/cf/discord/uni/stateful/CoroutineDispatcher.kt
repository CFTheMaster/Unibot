package com.github.cf.discord.uni.stateful

import java.util.concurrent.ExecutorService
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher

class CoroutineDispatcher(val pool: ExecutorService) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        pool.execute(block)
    }
}