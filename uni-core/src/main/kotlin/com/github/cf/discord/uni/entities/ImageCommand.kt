package com.github.cf.discord.uni.entities

import com.github.cf.discord.uni.utils.Http
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream


abstract class ImageCommand : Command() {
    abstract fun imageRun(ctx: Context, file: ByteArray)

    override fun run(ctx: Context) {
        when {
            ctx.msg.attachments.isNotEmpty() -> imageRun(ctx, ctx.msg.attachments[0].inputStream.readBytes())

            ctx.args.containsKey("image") -> Http.get(ctx.args["image"] as String).thenAccept { res ->
                imageRun(ctx, res.body()!!.bytes())
                res.close()
            }

            else -> ctx.getLastImage().thenAccept { img ->
                if (img == null) {
                    return@thenAccept ctx.send("image channel not found")
                }

                imageRun(ctx, img.readBytes())
            }
        }
    }
}