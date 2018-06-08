package com.github.cf.discord.uni.entities

data class Config(
        val id: String?,
        val token: String?,
        val description: String?,
        val owners: List<String?>,
        val prefixes: List<String?>
)