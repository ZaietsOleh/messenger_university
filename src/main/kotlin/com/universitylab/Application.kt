package com.universitylab

import com.universitylab.database.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.universitylab.plugins.*
import com.universitylab.plugins.socket.configureSockets
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val appEnv = KtorApplicationEnvironment(
        secret = "secret",
        issuer = "http://0.0.0.0:8080/",
        myRealm = "Access to 'Messenger'",
    )

    DatabaseFactory.init()
    configureSecurity(appEnv)
    configureSockets(json)
    configureSerialization()
    configureRouting(appEnv)
}

data class KtorApplicationEnvironment(
    val secret: String,
    val issuer: String,
    val myRealm: String,
)
