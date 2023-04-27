package com.universitylab.user.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignOnModel(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
)
