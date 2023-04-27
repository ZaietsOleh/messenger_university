package com.universitylab.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.universitylab.KtorApplicationEnvironment
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity(appEnv: KtorApplicationEnvironment) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = appEnv.myRealm
            verifier(
                JWT
                .require(Algorithm.HMAC256(appEnv.secret))
                .withIssuer(appEnv.issuer)
                .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
