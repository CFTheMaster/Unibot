package com.github.cf.discord.uni.stateful

import net.dv8tion.jda.core.events.Event
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.timerTask

class EventWaiter {
    internal val listeners = mutableMapOf<Class<*>, MutableList<EventListenerWrapper<*>>>()
    private val timeoutScheduler = Timer()

    /*fun<T : Event> listenEvent(listenerType: Class<T>, block: (T) -> Unit): EventListenerWrapper<T> {
        val wrapper = EventListenerWrapper(listenerType, block)
        listeners.add(wrapper)
        return wrapper
    }*/

    /*inline fun<reified T : Event> listen(noinline block: (T) -> Unit): EventListenerWrapper<T> {
        return listenEvent(T::class.java, block)
    }*/

    fun<T : Event> removeListener(listenerClass: Class<T>, listener: EventListenerWrapper<*>) {
        listeners[listenerClass]?.remove(listener)
    }

    fun<T : Event> emit(event: T) {
        val actuallyListeners = listeners[event::class.java] ?: return
        @Suppress("UNCHECKED_CAST")
        actuallyListeners.map { it as EventListenerWrapper<T> }.forEach {
            it.invoke(event)
        }
    }

    fun<T : Event> awaitEvent(listenerType: Class<T>, count: Int, timeout: Long, predicate: (T) -> Boolean): CompletableFuture<List<T>> {
        val fut = CompletableFuture<List<T>>()
        val list = mutableListOf<T>()
        val listener = TemporaryEventListenerWrapper(this, listenerType, { e: T, wrapper: EventListenerWrapper<T> ->
            if (fut.isCancelled) wrapper.remove(this)
            if (predicate(e)) list.add(e)
        }, { _: T, _: EventListenerWrapper<T> ->
            if (list.size >= count) {
                if (!fut.isCancelled) fut.complete(list)
                true
            } else false
        })
        if (timeout > 0) {
            timeoutScheduler.schedule(timerTask {
                if (!fut.isDone) fut.cancel(true)
            }, timeout)
        }
        if (listeners[listenerType] == null) listeners[listenerType] = mutableListOf()
        listeners[listenerType]?.add(listener)
        return fut
    }

    inline fun<reified T : Event> await(count: Int, timeout: Long, noinline predicate: (T) -> Boolean): CompletableFuture<List<T>> = awaitEvent(T::class.java, count, timeout, predicate)

    open class EventListenerWrapper<T : Event>(val type: Class<T>, private val target: (T, EventListenerWrapper<T>) -> Unit) {
        open operator fun invoke(payload: T) {
            target(payload, this)
        }
        open fun remove(waiter: EventWaiter) {
            waiter.removeListener(type, this)
        }
    }

    class TemporaryEventListenerWrapper<T : Event>(private val waiter: EventWaiter, type: Class<T>, private val target: (T, EventListenerWrapper<T>) -> Unit, private val predicate: (T, EventListenerWrapper<T>) -> Boolean) : EventListenerWrapper<T>(type, target) {
        override operator fun invoke(payload: T) {
            target(payload, this)
            if (predicate(payload, this)) {
                super.remove(waiter)
            }
        }
    }
}