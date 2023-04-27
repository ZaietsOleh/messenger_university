package com.universitylab.message.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UpdateMessage(
    @SerialName("messageId") val messageId: Int,
    @SerialName("message") val message: String,
)
