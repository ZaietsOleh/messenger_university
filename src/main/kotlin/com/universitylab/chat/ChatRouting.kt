package com.universitylab.chat

import com.universitylab.chat.models.ChatMember
import com.universitylab.chat.models.NewChat
import com.universitylab.database.*
import com.universitylab.database.Chat
import com.universitylab.database.Member
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

fun Routing.chatRouting() {
    authenticate("auth-jwt") {
        post("/createChat") {
            try {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId")?.asInt()!!
                val newChatModel = call.receive<NewChat>()

                if (newChatModel.name.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Chat name is empty!")
                    return@post
                }

                transaction {
                    val chatId = Chats.insert {
                        it[Chats.name] = newChatModel.name
                        it[Chats.creatorId] = userId
                    }[Chats.id]

                    newChatModel.users.forEach { newUserId ->
                        Members.insert {
                            it[Members.chatId] = chatId
                            it[Members.userId] = newUserId
                        }
                    }
                }
                MessengerDbStorage.chatsUpdated()
                MessengerDbStorage.membersUpdated()

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong! ${e.message}")
            }
        }

        delete("/deleteChat/{chatId}") {
            try {
                val chatId = call.parameters["chatId"]?.toIntOrNull() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest, "Incorrect chat id!")
                    return@delete
                }

                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId")?.asInt()!!

                if (MessengerDbStorage.getChats().firstOrNull { it.id == chatId }?.creatorId != userId) {
                    call.respond(HttpStatusCode.BadRequest, "You are not creator of this chat!")
                    return@delete
                }

                transaction {
                    Chats.deleteWhere { Chats.id eq chatId }
                }
                MessengerDbStorage.chatsUpdated()
                MessengerDbStorage.membersUpdated()

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
            }
        }

        delete("/leaveChat/{chatId}") {
            try {
                val chatId = call.parameters["chatId"]?.toIntOrNull() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest, "Incorrect chat id!")
                    return@delete
                }

                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId")?.asInt()!!

                if (MessengerDbStorage.getMembers().firstOrNull { it.userId == userId && it.chatId == chatId } == null) {
                    call.respond(HttpStatusCode.BadRequest, "You are not member of this chat!")
                    return@delete
                }

                transaction {
                    Members.deleteWhere { Members.chatId eq chatId and (Members.userId eq userId) }
                }
                MessengerDbStorage.chatsUpdated()
                MessengerDbStorage.membersUpdated()

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
            }
        }

        post("/addUserToChat") {
            try {
                val chatMember = call.receive<ChatMember>()

                if (MessengerDbStorage.getMembers().firstOrNull { it.userId == chatMember.userId && it.chatId == chatMember.chatId } != null) {
                    call.respond(HttpStatusCode.BadRequest, "User is already member of this chat!")
                    return@post
                }

                if (MessengerDbStorage.getChats().firstOrNull { it.id == chatMember.chatId } == null) {
                    call.respond(HttpStatusCode.BadRequest, "Chat not found!")
                    return@post
                }

                if (MessengerDbStorage.getUsers().firstOrNull { it.id == chatMember.userId } == null) {
                    call.respond(HttpStatusCode.BadRequest, "User not found!")
                    return@post
                }

                transaction {
                    Members.insert {
                        it[Members.chatId] = chatMember.chatId
                        it[Members.userId] = chatMember.userId
                    }
                }
                MessengerDbStorage.membersUpdated()

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
            }
        }

        delete("/removeUserFromChat") {
            try {
                val chatMember = call.receive<ChatMember>()
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId")?.asInt()!!

                if (MessengerDbStorage.getChats().firstOrNull { it.id == chatMember.chatId }?.creatorId != userId) {
                    call.respond(HttpStatusCode.BadRequest, "You are not creator of this chat!")
                    return@delete
                }

                transaction {
                    Members.deleteWhere { Members.chatId eq chatMember.chatId and (Members.userId eq chatMember.userId) }
                }
                MessengerDbStorage.membersUpdated()

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
            }
        }
    }
}
