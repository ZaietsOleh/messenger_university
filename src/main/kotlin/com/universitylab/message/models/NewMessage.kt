package com.universitylab.message.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NewMessage(
    @SerialName("chatId") val chatId: Int,
    @SerialName("message") val message: String,
    @SerialName("repliedTo") val repliedTo: Int? = null,
)
