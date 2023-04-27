package com.universitylab.message

import com.universitylab.database.MessengerDbStorage
import com.universitylab.database.Messages
import com.universitylab.message.models.NewMessage
import com.universitylab.message.models.UpdateMessage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun Routing.messageRouting() {
    authenticate("auth-jwt") {
        post("/sendMessage") {
            try {
                val message = call.receive<NewMessage>()
                val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId")?.asInt()!!

                if (MessengerDbStorage.getMembers().firstOrNull { it.userId == currentUserId && it.chatId == message.chatId } == null) {
                    call.respond(HttpStatusCode.BadRequest, "You are not a member of this chat!")
                    return@post
                }

                if (message.repliedTo != null && MessengerDbStorage.getMessages().firstOrNull { it.id == message.repliedTo } == null) {
                    call.respond(HttpStatusCode.BadRequest, "Message not found!")
                    return@post
                }

                if (MessengerDbStorage.getChats().firstOrNull { it.id == message.chatId } == null) {
                    call.respond(HttpStatusCode.BadRequest, "Chat not found!")
                    return@post
                }

                transaction {
                    Messages.insert {
                        it[Messages.chatId] = message.chatId
                        it[Messages.userId] = currentUserId
                        it[Messages.message] = message.message
                        it[Messages.timestamp] = System.currentTimeMillis()
                        it[Messages.repliedTo] = message.repliedTo
                    }
                }
                MessengerDbStorage.messagesUpdated()

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
            }
        }

        delete("/deleteMessage/{messageId}") {
            try {
                val messageId = call.parameters["messageId"]?.toIntOrNull() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest, "Incorrect message id!")
                    return@delete
                }

                val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId")?.asInt()!!

                if (MessengerDbStorage.getMessages().firstOrNull { it.id == messageId } == null) {
                    call.respond(HttpStatusCode.BadRequest, "Message not found!")
                    return@delete
                }

                if (MessengerDbStorage.getMembers().firstOrNull { it.userId == currentUserId && it.chatId == MessengerDbStorage.getMessages().first { it.id == messageId }.chatId } == null) {
                    call.respond(HttpStatusCode.BadRequest, "You are not a member of this chat!")
                    return@delete
                }

                if (MessengerDbStorage.getMessages().firstOrNull { it.id == messageId }?.userId != currentUserId) {
                    call.respond(HttpStatusCode.BadRequest, "You can't delete this message!")
                    return@delete
                }

                transaction {
                    Messages.deleteWhere { Messages.id eq messageId }
                }
                MessengerDbStorage.messagesUpdated()

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
            }
        }

        post("/updateMessage") {
            try {
                val updatedMessage = call.receive<UpdateMessage>()
                val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId")?.asInt()!!

                if (MessengerDbStorage.getMessages().firstOrNull { it.id == updatedMessage.messageId }?.userId != currentUserId) {
                    call.respond(HttpStatusCode.BadRequest, "You can't update this message!")
                    return@post
                }

                transaction {
                    Messages.update({ Messages.id eq updatedMessage.messageId }) {
                        it[Messages.message] = updatedMessage.message
                    }
                }
                MessengerDbStorage.messagesUpdated()

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
            }
        }
    }
}
