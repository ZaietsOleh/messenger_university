package com.universitylab.plugins.socket

import com.universitylab.database.MessengerDbStorage
import com.universitylab.database.User
import com.universitylab.chat.GetChatMembersSubscription
import com.universitylab.chat.GetChatsSubscription
import com.universitylab.message.GetChatMessagesSubscription
import com.universitylab.plugins.socket.Subscriptions.Companion.generateSubscriptionKey
import com.universitylab.user.GetContactsSubscription
import io.ktor.http.*
import io.ktor.server.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.Exception

val sessions = mutableListOf<SocketSession>()

fun Application.configureSockets(json: Json) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        authenticate("auth-jwt") {
            webSocket {

                val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId")?.asInt()!!
                val user = MessengerDbStorage.getUsers().firstOrNull { it.id == currentUserId } ?: kotlin.run {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "User not registered!"))
                    return@webSocket
                }

                val socketSession = SocketSession(user = user, session = this)
                sessions.add(socketSession)
                val subscriptions = mutableMapOf<String, SubscriptionProcessor>()

                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            try {
                                val subscription = json.decodeFromString<Subscription>(frame.readText())
                                when (subscription.getType()) {
                                    IncomingTypes.SUBSCRIBE -> {
                                        resolveSubscription(json, subscription, socketSession)?.let { subsToProcessor ->
                                            subscriptions[subsToProcessor.first]?.unsubscribe()
                                            subscriptions.plus(subsToProcessor)
                                            subsToProcessor.second.subscribe()
                                        }
                                    }

                                    IncomingTypes.UNSUBSCRIBE -> {
                                        subscription.getSubscription()?.let {
                                            subscriptions.remove(it.generateSubscriptionKey(subscription.params))?.unsubscribe()
                                        }
                                    }

                                    else -> Unit
                                }
                            } catch (e: Exception) {
                                call.respond(HttpStatusCode.BadRequest, "Incorrect subscription!")
                            }
                        }

                        is Frame.Close -> {
                            socketSession.send("Close!")
                            subscriptions.forEach { (_, processor) ->
                                processor.unsubscribe()
                            }
                            sessions.remove(socketSession)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}

private fun resolveSubscription(
    json: Json,
    subscription: Subscription,
    socketSession: SocketSession,
): Pair<String, SubscriptionProcessor>? {
    return when (val subs = subscription.getSubscription()) {
        Subscriptions.GET_CHATS -> subs.generateSubscriptionKey(subscription.params) to GetChatsSubscription(json, socketSession)
        Subscriptions.GET_CHAT_MESSAGES -> subs.generateSubscriptionKey(subscription.params) to GetChatMessagesSubscription(subscription.params, json, socketSession)
        Subscriptions.GET_CONTACTS -> subs.generateSubscriptionKey(subscription.params) to GetContactsSubscription(json, socketSession)
        Subscriptions.GET_CHAT_MEMBERS -> subs.generateSubscriptionKey(subscription.params) to GetChatMembersSubscription(subscription.params, json, socketSession)
        else -> null
    }
}

class SocketSession(
    val user: User,
    val session: DefaultWebSocketServerSession,
) {
    suspend fun send(data: String) {
        session.outgoing.send(Frame.Text(data))
    }
}
