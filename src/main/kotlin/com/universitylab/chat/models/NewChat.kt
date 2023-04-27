package com.universitylab.chat.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewChat(
    @SerialName("name") val name: String,
    @SerialName("users") val users: List<Int>,
)
