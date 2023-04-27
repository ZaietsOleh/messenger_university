package com.universitylab.chat.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMember(
    @SerialName("userId") val userId: Int,
    @SerialName("chatId") val chatId: Int,
)
