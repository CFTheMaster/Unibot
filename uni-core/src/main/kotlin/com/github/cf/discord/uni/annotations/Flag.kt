package com.github.cf.discord.uni.annotations

annotation class Flag(val flag: String, val abbr: Char, val desc: String)

annotation class Flags(vararg val flags: Flag)