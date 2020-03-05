package com.wwt.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonFactory {
    val gson = gsonWithDate()
}

fun gsonWithDate(): Gson {
    val builder = GsonBuilder()
    return builder.create()
}