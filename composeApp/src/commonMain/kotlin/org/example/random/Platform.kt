package org.example.random

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform