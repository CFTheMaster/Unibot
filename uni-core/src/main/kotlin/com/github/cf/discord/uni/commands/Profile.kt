/*
 *   Copyright (C) 2017-2021 computerfreaker
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
import net.dv8tion.jda.api.entities.Member
import org.jetbrains.exposed.sql.select
import org.joda.time.DateTime
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.schedule

@Load
@Argument("user", "user", true)
@Alias("viewxp", "xp")
class Profile : Command(){
    override val guildOnly = true
    override val desc = "View someone's profile!"

    private fun Image.toBufferedImage(): BufferedImage {
        try{
            if (this is BufferedImage) {
                return this
            }
            val bufferedImage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)

            val graphics2D = bufferedImage.createGraphics()
            graphics2D.drawImage(this, 0, 0, null)
            graphics2D.dispose()

            return bufferedImage
        } catch (e: IOException){
            println(e)
        }
        return error("help")
    }

    private fun processImg(ctx: Context, userXPPoints: Long, totalExp: Long ,xpNeeded: Float, progress: Float, level: Long, member: Member, lastLevelUp: DateTime, userCreationDate: DateTime){

        try {

            val avatarUrl = member.user.avatarUrl.orEmpty()
            val getImage = URL(avatarUrl).openConnection()
            getImage.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")

            val getBG = URL("https://computerfreaker.pw/uni/20201124174001.jpg").openConnection()
            getBG.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")


            val img = ImageIO.read(getBG.getInputStream()).toBufferedImage()
            val profilePicture = ImageIO.read(getImage.getInputStream()).toBufferedImage()

            val g2d: Graphics2D = img.graphics as Graphics2D

            val x = 50f

            val y = 230f

            val progressWidth = 700f
            val progressHeight = 50f

            val xpCurrentText = "Current: $userXPPoints Out Of ${xpNeeded.toLong()}"
            val username = "${member.user.name}#${member.user.discriminator}"
            // val lastLevelUpText = "Last level-up: ${lastLevelUp.toString("EEEE yyyy MMMM dd HH:mm:ss.SSS", Locale.US)}"
            val levelString = "Lv.: $level"
            val totalExpString = "Total Exp: $totalExp"

            g2d.setColor(Color(255, 255, 255, 120))
            g2d.fillRect((x+200).toInt(), (y +(60*3-10)- 35).toInt(), (progressWidth).toInt(), progressHeight.toInt())



            val stringHeight = g2d.getFontMetrics(g2d.font).height

            g2d.setColor(Color(0,120,0, 255))
            g2d.fillRect( (x+200).toInt(), (y+(60*3-10) - 35).toInt(), (progressWidth * (progress / (100).toDouble())).toInt(), progressHeight.toInt())

            g2d.setColor(Color(0, 120, 0, 160))
            g2d.drawRect((x+200).toInt(), (y +(60*3-10)- 35).toInt(), (progressWidth).toInt(), progressHeight.toInt())

            g2d.setColor(Color(0, 0, 255, 255))
            g2d.fillRect((x+200).toInt(), (y +(60*4-10)- 35).toInt(), 700, 50)

            //val stringWidth2 = g2d.getFontMetrics(g2d.font).stringWidth(lastLevelUpText)

            val gradiantColor: Paint = GradientPaint(0f,0f, Color(102,0,204, 255), 1280f, 132f, Color(204, 0, 102), true )

            g2d.setPaint(gradiantColor)
            g2d.setStroke(BasicStroke(4f))
            g2d.fillRect(0, 0, 1280, 132)

            g2d.setColor(Color(255,255,255))
            g2d.setFont(Font(Font.SANS_SERIF, Font.BOLD, 40))
            g2d.drawString(username, 140, 40)

            g2d.setColor(Color(255,255,255))
            g2d.setFont(Font(Font.SANS_SERIF, Font.BOLD, 24))
            g2d.drawString(totalExpString, 140, 75)

            g2d.setColor(Color(1,1,1, 200))
            g2d.setFont(Font(Font.SANS_SERIF, Font.BOLD, 40))
            g2d.drawString("${progress.toInt()}%" , (x+730f), y +(60*3-10) + 2)

            g2d.setColor(Color(1,1,1, 200))
            g2d.setFont(Font(Font.SANS_SERIF, Font.BOLD, 40))
            g2d.drawString(levelString , (x+250f), y +(60*3-10) + 2)

            g2d.setColor(Color(255,255,0))
            g2d.setFont(Font(Font.SANS_SERIF, Font.BOLD, 30))
            g2d.drawString(xpCurrentText, x+250, y+(60*4-10))

            g2d.drawImage(profilePicture, 1,  1, null)

            g2d.setColor(Color(1,1,1, 0))
            g2d.draw(Rectangle(100, 100))

            g2d.setColor(Color(1,1,1, 255))
            g2d.setStroke(BasicStroke(4f))
            g2d.draw(Rectangle(2, 2, 129, 129))



            g2d.finalize()

            g2d.dispose()

            val writing = ImageIO.write(img, "png", File("src/main/resources/profile/${member.idLong}_profile.png"))

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

                if(level == 1.toLong()){
                    val xpNeeded =  (level.toFloat() * 50f) * (MINIMUM_FOR_LEVEL_1 * (level.toFloat() / 3f))
                    val progress = xp.toFloat() / xpNeeded * 100f

                    processImg(ctx, xp, xp,xpNeeded, progress, level, member,contract[Users.lastLevelUp], contract[Users.accountCreationDate])
                } else {

                    val previousLevel = level - 1

                    val xpNeeded = (level.toFloat() * 50f) * (MINIMUM_FOR_LEVEL_1 * (level.toFloat() / 3f))
                    val xpFromLastLevel =  (previousLevel.toFloat() - 1f) * 50f * (MINIMUM_FOR_LEVEL_1 * (previousLevel.toFloat() / (3f)))
                    val progress = (xp.toFloat() - xpFromLastLevel) / (xpNeeded - xpFromLastLevel) * 100f
                    val curExp = xp.toFloat() - xpFromLastLevel
                    val expTillLevel = xpNeeded - xpFromLastLevel


                    processImg(ctx, curExp.toLong(), xp, expTillLevel, progress, level, member ,contract[Users.lastLevelUp], contract[Users.accountCreationDate])
                }




        }.execute()

        Timer().schedule(2000){
            val profileImg = File("src/main/resources/profile/${member.idLong}_profile.png")

            ctx.channel.sendFile(profileImg).complete()

            profileImg.delete()
        }



    }
}
