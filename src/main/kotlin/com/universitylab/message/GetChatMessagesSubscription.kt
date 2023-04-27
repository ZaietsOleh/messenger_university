package com.universitylab.message

import com.universitylab.database.MessengerDbStorage
import com.universitylab.database.Message
import com.universitylab.plugins.socket.SocketSession
import com.universitylab.plugins.socket.SubscriptionProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class GetChatMessagesSubscription(
    private val params: List<String>,
    private val json: Json,
    private val session: SocketSession,
) : SubscriptionProcessor {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun subscribe() {
        params.firstOrNull()?.toIntOrNull()?.let { chatId ->
            MessengerDbStorage.messagesFlow
                .mapLatest {
                    it.filter { message -> message.chatId == chatId }
                        .sortedBy(Message::timestamp)
                }
                .distinctUntilChanged()
                .onEach {
                    session.send(json.encodeToString(it))
                }
                .launchIn(scope)
        }
    }

    override fun unsubscribe() {
        scope.cancel()
    }

}
