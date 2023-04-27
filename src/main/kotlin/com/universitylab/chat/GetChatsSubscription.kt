package com.universitylab.chat

import com.universitylab.database.MessengerDbStorage
import com.universitylab.plugins.socket.SocketSession
import com.universitylab.plugins.socket.SubscriptionProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class GetChatsSubscription(
    private val json: Json,
    private val session: SocketSession,
) : SubscriptionProcessor {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun subscribe() {
        MessengerDbStorage.chatsFlow
            .combine(MessengerDbStorage.membersFlow) { allChats, chatsMembers ->
                val userChats = chatsMembers.filter { it.userId == session.user.id }.map { it.chatId }
                allChats.filter { userChats.contains(it.id) }
            }
            .distinctUntilChanged()
            .onEach {
                session.send(json.encodeToString(it))
            }
            .launchIn(scope)
    }

    override fun unsubscribe() {
        scope.cancel()
    }

}
