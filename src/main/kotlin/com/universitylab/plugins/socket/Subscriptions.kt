package com.universitylab.plugins.socket

internal enum class Subscriptions(val value: String) {
    GET_CHAT_MESSAGES("GetChatMessages"),
    GET_CHATS("GetChats"),
    GET_CONTACTS("GetContacts"),
    GET_CHAT_MEMBERS("GetChatMembers");

    companion object {
        fun fromString(name: String): Subscriptions? {
            return values().firstOrNull { it.value == name }
        }

        fun Subscriptions.generateSubscriptionKey(params: List<String>): String {
            return "${this.name}_${params.joinToString("_")}"
        }
    }
}
