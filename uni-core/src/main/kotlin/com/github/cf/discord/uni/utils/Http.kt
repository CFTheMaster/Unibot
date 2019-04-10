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