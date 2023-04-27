package com.universitylab.plugins.socket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Subscription(
    @SerialName("type") val type: String,
    @SerialName("name") val name: String,
    @SerialName("params") val params: List<String> = emptyList(),
) {
    internal fun getType(): IncomingTypes? {
        return IncomingTypes.fromString(type)
    }

    internal fun getSubscription(): Subscriptions? {
        return Subscriptions.fromString(name)
    }
}
