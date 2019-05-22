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
