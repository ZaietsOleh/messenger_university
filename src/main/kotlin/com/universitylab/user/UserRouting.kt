package com.universitylab.user

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.universitylab.*
import com.universitylab.database.*
import com.universitylab.user.models.SignOnModel
import com.universitylab.user.models.SignUpModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Routing.userRouting(appEnv: KtorApplicationEnvironment) {
    post("/signUp") {
        try {
            val newUser = call.receive<SignUpModel>()

            if (MessengerDbStorage.getUsers().firstOrNull { it.username == newUser.username } != null) {
                call.respond(HttpStatusCode.BadRequest, "User already exists!")
                return@post
            }

            if (MessengerDbStorage.getUsers().firstOrNull { it.email == newUser.email } != null) {
                call.respond(HttpStatusCode.BadRequest, "Email already exists!")
                return@post
            }

            if (MessengerDbStorage.getUsers().firstOrNull { it.phone == newUser.phone } != null) {
                call.respond(HttpStatusCode.BadRequest, "Phone already exists!")
                return@post
            }

            transaction {
                Users.insert {
                    it[Users.username] = newUser.username
                    it[Users.email] = newUser.email
                    it[Users.password] = newUser.password
                    it[Users.phone] = newUser.phone
                }
            }
            MessengerDbStorage.usersUpdated()

            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
        }
    }

    post("/signOn") {
        try {
            val newUser = call.receive<SignOnModel>()
            MessengerDbStorage.getUsers().firstOrNull { it.username == newUser.username && it.password == newUser.password }?.let { logUser ->
                val token = JWT.create()
                    .withIssuer(appEnv.issuer)
                    .withClaim("userId", logUser.id)
                    .withExpiresAt(Date(System.currentTimeMillis() + 3600000)) // 1 hour
                    .sign(Algorithm.HMAC256(appEnv.secret))

                transaction {
                    Sessions.deleteWhere { Sessions.userId eq logUser.id }
                    Sessions.insert {
                        it[Sessions.userId] = logUser.id
                        it[Sessions.token] = token
                    }
                }

                call.respond(HttpStatusCode.OK, "Bearer $token")
            } ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "User not registered or incorrect input data!")
            }

            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
        }
    }

    authenticate("auth-jwt") {
        delete("/removeContact/{userId}") {
            try {
                val userId = call.parameters["userId"]?.toIntOrNull() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest, "Incorrect user id!")
                    return@delete
                }

                val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId")?.asInt()!!

                transaction {
                    Contacts.deleteWhere { Contacts.userId eq currentUserId and (Contacts.contactId eq userId) }
                }
                MessengerDbStorage.contactsUpdated()

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
            }
        }

        post("/addContact/{username}") {
            try {
                val username = call.parameters["username"] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest, "Incorrect username!")
                    return@post
                }

                val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId")?.asInt()!!
                val contactId = MessengerDbStorage.getUsers().firstOrNull { it.username == username }?.id ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest, "User not found!")
                    return@post
                }

                transaction {
                    Contacts.insert {
                        it[Contacts.userId] = currentUserId
                        it[Contacts.contactId] = contactId
                    }
                }
                MessengerDbStorage.contactsUpdated()

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong!")
            }
        }
    }
}
