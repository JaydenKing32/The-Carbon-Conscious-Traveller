package com.mquniversity.tcct.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform