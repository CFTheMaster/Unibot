package com.github.cf.discord.uni.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object CFApi{
    fun getCFApi(name: String): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://api.computerfreaker.cf/v1/$name")
                .build()).execute()

        return if (response.isSuccessful) {
            val content = JSONObject(response.body()?.string())
            response.body()?.close()
            content.getString("url")
        } else {
            response.body()?.close()
            null
        }
    }
}