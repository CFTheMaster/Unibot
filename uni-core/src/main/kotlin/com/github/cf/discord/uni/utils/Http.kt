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
package com.github.cf.discord.uni.utils

import okhttp3.*
import java.io.IOException
import java.util.concurrent.CompletableFuture

object Http {
    val okhttp = OkHttpClient()

    inline fun get(url: HttpUrl, block: Request.Builder.() -> Unit = {}): CompletableFuture<Response> {
        val fut = CompletableFuture<Response>()

        okhttp.newCall(Request.Builder().apply {
            get()
            url(url)

            block()
        }.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                fut.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                fut.complete(response)
            }
        })

        return fut
    }

    inline fun get(url: String, block: Request.Builder.() -> Unit = {}): CompletableFuture<Response> {
        val httpUrl = HttpUrl.parse(url) ?: return CompletableFuture<Response>().apply { completeExceptionally(Exception("Invalid URL")) }

        return get(httpUrl, block)
    }

    inline fun post(url: HttpUrl, body: RequestBody, block: Request.Builder.() -> Unit = {}): CompletableFuture<Response> {
        val fut = CompletableFuture<Response>()

        okhttp.newCall(Request.Builder().apply {
            post(body)
            url(url)

            block()
        }.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                fut.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                fut.complete(response)
            }
        })

        return fut
    }

    inline fun post(url: String, body: RequestBody, block: Request.Builder.() -> Unit = {}): CompletableFuture<Response> {
        val httpUrl = HttpUrl.parse(url) ?: return CompletableFuture<Response>().apply { completeExceptionally(Exception("Invalid URL")) }

        return post(httpUrl, body, block)
    }
}
