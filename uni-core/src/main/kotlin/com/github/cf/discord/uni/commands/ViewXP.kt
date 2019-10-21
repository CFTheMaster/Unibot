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

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.Uni.Companion.MINIMUM_FOR_LEVEL_1
import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.database.schema.Users
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import org.jetbrains.exposed.sql.select
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.awt.*
import java.awt.image.BufferedImage
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.concurrent.schedule

@Load
@Argument("user", "user", true)
@Alias("profile")
class ViewXP : Command(){
    override val guildOnly = true
    override val desc = "View someone's xp!"

    private fun Image.toBufferedImage(): BufferedImage {
        if (this is BufferedImage) {
            return this
        }
        val bufferedImage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        val graphics2D = bufferedImage.createGraphics()
        graphics2D.drawImage(this, 0, 0, null)
        graphics2D.dispose()

        return bufferedImage
    }

    private fun processImg(ctx: Context, userXPPoints: Long, xpNeeded: Double, progress: Double, lastLevelUp: DateTime, userCreationDate: DateTime){

        try {
            val img = ImageIO.read(URL("https://i.ytimg.com/vi/xHjgZX4oQa8/maxresdefault.jpg")).toBufferedImage()

            val g2d: Graphics2D = img.graphics as Graphics2D

            g2d.setColor(Color(255, 255, 255, 125))
            g2d.fillRect(40, 200,1150,310)

            g2d.setColor(Color.BLACK)
            g2d.setFont(Font(Font.SANS_SERIF, Font.PLAIN, 40))
            g2d.drawString("Username: ${ctx.author.name}#${ctx.author.discriminator}", 40f, 250f)

            g2d.setColor(Color(195, 5, 100))
            g2d.setFont(Font(Font.SANS_SERIF, Font.PLAIN, 40))
            g2d.drawString("Experience Points: ${userXPPoints}/${xpNeeded.toLong()}", 40f, 310f)

            g2d.setColor(Color(200, 23, 12))
            g2d.setFont(Font(Font.SANS_SERIF, Font.PLAIN, 40))
            g2d.drawString("User last level-up: ${lastLevelUp.toString("E yyyy/MM/dd HH:mm:ss.SSS")}", 40f, 370f)

            g2d.setColor(Color(102, 0, 204))
            g2d.setFont(Font(Font.SANS_SERIF, Font.PLAIN, 40))
            g2d.drawString("User creation date: ${userCreationDate.toString("E yyyy/MM/dd HH:mm:ss.SSS")}", 40f, 430f)

            g2d.setColor(Color(0, 13, 255))
            g2d.setFont(Font(Font.SANS_SERIF, Font.PLAIN, 40))
            g2d.drawString("Progress: ${"#".repeat(progress.toInt())}${"-".repeat(10 - progress.toInt())} ${progress.toInt() * 10}%" , 40f, 490f)

            g2d.finalize()

            val writing = ImageIO.write(img, "png", File("src/main/resources/profile/${ctx.author.idLong}_profile.png"))

            if (writing){
                println("writing is busy")
            }

            img.flush()

            println("image is done processing")

        } catch (e: IOException){
            println(e)
        }
    }


    override fun run(ctx: Context) {

        val member = ctx.args.getOrDefault("user", ctx.member!!) as Member

        asyncTransaction(Uni.pool){

            val contract = Users.select{ Users.id.eq(member.user.idLong)}.firstOrNull()
                    ?: return@asyncTransaction ctx.send(
                            if (!member.user.isBot) "user has no xp: ${member.user.name+"#"+member.user.discriminator+" (${member.user.idLong})"}" else "bots don't have exp"
                    )


                val xp = contract[Users.expPoints]
                val level = contract[Users.level]

                val xpNeeded = level.toDouble() * (500).toDouble() + (level.toDouble() * MINIMUM_FOR_LEVEL_1.toDouble())
                val progress = xp.toDouble() / xpNeeded * (10).toDouble()

                processImg(ctx, contract[Users.expPoints], xpNeeded, progress, contract[Users.lastLevelUp], contract[Users.accountCreationDate])

        }.execute()

        Timer().schedule(3000){
            val profileImg = File("src/main/resources/profile/${ctx.author.idLong}_profile.png")

            ctx.channel.sendFile(profileImg).completeAfter(6, TimeUnit.SECONDS)
        }



    }
}
