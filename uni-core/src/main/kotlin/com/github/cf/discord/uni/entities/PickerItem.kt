package com.github.cf.discord.uni.entities

import java.awt.Color

data class PickerItem (
        val id: String,
        val title: String = "",
        val description: String = "",
        val author: String = "",
        var image: String = "",
        val thumbnail: String = "",
        val footer: String = "",
        val color: Color? = null,
        val url: String = ""
)