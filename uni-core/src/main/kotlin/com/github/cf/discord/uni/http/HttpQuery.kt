/*
 *   Copyright (C) 2017-2020 computerfreaker
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
package com.github.cf.discord.uni.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.IOException

object HttpQuery {

    @JvmStatic
    val OBJECT_MAPPER: ObjectMapper = ObjectMapper()
            .registerModule(KotlinModule())
            .enable(SerializationFeature.INDENT_OUTPUT)

    @JvmStatic
    val HTTP_CLIENT: OkHttpClient = OkHttpClient()

    /**
     * Create an HTTP response Mono based on the supplied request
     */
    fun queryMono(request: Request): Mono<Response> = Mono.create<Response> {
        HTTP_CLIENT.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                it.error(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    it.success(response)
                } else {
                    it.error(HttpError(response.code(), "The HTTP request completed but did not return successfully."))
                }
            }
        })
    }.publishOn(Schedulers.elastic())

    /**
     * Transform a supplied HTTP response into a response body Mono
     */
    fun responseBody(response: Response): Mono<ResponseBody> = Mono.using({ response.body() }, { body: ResponseBody? -> Mono.justOrEmpty(body) }, { body: ResponseBody? -> body?.close() })
}
