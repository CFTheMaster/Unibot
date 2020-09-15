@file:JvmName("MiscUtil")
@file:Suppress("Unused", "FunctionName", "NOTHING_TO_INLINE")
package com.github.cf.discord.uni.extensions

inline fun <reified T> T.modifyIf(condition: (T) -> Boolean, block: (T) -> T): T = modifyIf(condition(this), block)
inline fun <reified T> T.modifyIf(condition: Boolean, block: (T) -> T): T = if(condition) block(this) else this