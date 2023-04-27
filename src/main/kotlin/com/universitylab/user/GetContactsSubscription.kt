package com.universitylab.user

import com.universitylab.database.MessengerDbStorage
import com.universitylab.plugins.socket.SocketSession
import com.universitylab.plugins.socket.SubscriptionProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GetContactsSubscription(
    private val json: Json,
    private val session: SocketSession,
) : SubscriptionProcessor {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun subscribe() {
        MessengerDbStorage.contactsFlow
            .mapLatest { contacts ->
                contacts.filter { it.userId == session.user.id }
            }
            .onEach {
                session.send(json.encodeToString(it))
            }
            .launchIn(scope)
    }

    override fun unsubscribe() {
        scope.cancel()
    }

}
