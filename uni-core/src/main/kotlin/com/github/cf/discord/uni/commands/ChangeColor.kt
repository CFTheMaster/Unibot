/*
 *   Copyright (C) 2017-2019 computerfreaker
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import java.awt.Color

@Load
@Argument("color", "string", true)
class ChangeColor : Command(){
    override val desc = "change your own color"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val sp = ctx.args["color"] as? String

        val user = ctx.guild!!.getMember(ctx.author)
        val serverRoles = ctx.guild.roles
        val userRoleIds = ctx.guild.getMember(ctx.author).roles

        val roleId = serverRoles.find { it.name.startsWith("#${sp!!.replace("#", "")}") }?.idLong

        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()


        if(sp == null){
            ctx.guild.controller.removeRolesFromMember(user, serverRoles.find { it.name.startsWith("#", true) }).queue()
            ctx.send("I have rermoved the color roles")
        }
        else if(sp == "something"){
            val color = Color(randomColor, randomColor1, randomColor2)
            val colorName = java.lang.Integer.toHexString(color.rgb).replaceFirst("ff", "")
            ctx.guild.controller.removeRolesFromMember(user, serverRoles.find { it.name.startsWith("#", true) }).queue()
            val newRole = ctx.guild.controller.createRole().setColor(color).setName("#$colorName").complete()
            ctx.guild.controller.addSingleRoleToMember(user, newRole).queue()
            ctx.send("gave you a random color")
        } else{
            if(roleId == null){
                val ohShit = java.lang.Integer.parseInt(sp.replace("#", ""), 16)
                val hexAss = java.lang.Integer.toHexString(ohShit)
                val color = Color(ohShit)
                ctx.guild.controller.removeRolesFromMember(user, serverRoles.find { it.name.startsWith("#", true) }).queue()
                val haha = ctx.guild.controller.createRole().setColor(color).setName("#$hexAss").complete()
                ctx.guild.controller.addSingleRoleToMember(user, haha).queue()
                ctx.send("<@!${ctx.author.idLong}> changed your color!")
            }
            else if(userRoleIds.any({it.idLong == roleId})){
                ctx.send("<@!${ctx.author.idLong}>You already have this color!")
            }
            else{
                val role = ctx.guild.getRoleById(roleId)
                ctx.guild.controller.addSingleRoleToMember(ctx.guild.getMember(ctx.author), role).queue()
                ctx.send("<@!${ctx.author.idLong}> changed your color!")
            }
        }
    }
}
