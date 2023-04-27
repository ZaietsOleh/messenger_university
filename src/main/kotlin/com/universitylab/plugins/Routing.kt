package com.universitylab.plugins

import com.universitylab.KtorApplicationEnvironment
import com.universitylab.chat.chatRouting
import com.universitylab.message.messageRouting
import com.universitylab.user.userRouting
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting(appEnv: KtorApplicationEnvironment) {
    routing {
        messageRouting()
        chatRouting()
        userRouting(appEnv)
    }
}
