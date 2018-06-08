package com.github.cf.discord.uni.annotations

@Repeatable
annotation class Argument(val name: String, val type: String, val optional: Boolean = false)

annotation class Arguments(vararg val args: Argument)