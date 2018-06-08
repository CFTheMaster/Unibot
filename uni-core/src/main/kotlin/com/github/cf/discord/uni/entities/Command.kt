package com.github.cf.discord.uni.entities

abstract class Command {
    val subcommands = mutableMapOf<String, Command>()
    open val name = ""
    open val desc = ""
    open val ownerOnly = false
    open val noHelp = false
    open val guildOnly = false
    open val nsfw = false
    open val cooldown = 5

    abstract fun run(ctx: Context)

    fun addSubcommand(cmd: Command, name: String? = null) {
        subcommands[(name ?: if (cmd.name.isEmpty()) cmd::class.simpleName ?: return else cmd.name).toLowerCase()] = cmd
    }
}