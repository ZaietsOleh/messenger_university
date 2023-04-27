package com.universitylab.user.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignUpModel(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("phone") val phone: String,
    @SerialName("email") val email: String,
)
