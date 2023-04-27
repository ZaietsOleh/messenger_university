package com.universitylab.database

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object MessengerDbStorage {

    internal val chatsFlow: MutableStateFlow<List<Chat>> = MutableStateFlow(emptyList())

    internal val messagesFlow: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())

    internal val membersFlow: MutableStateFlow<List<Member>> = MutableStateFlow(emptyList())

    internal val usersFlow: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())

    internal val contactsFlow: MutableStateFlow<List<Contact>> = MutableStateFlow(emptyList())

    internal fun chatsUpdated() {
        chatsFlow.update {
            transaction {
                Chats.selectAll().map {
                    Chat(
                        id = it[Chats.id],
                        name = it[Chats.name],
                        creatorId = it[Chats.creatorId],
                    )
                }
            }
        }
    }

    internal fun messagesUpdated() {
        messagesFlow.update {
            transaction {
                Messages.selectAll().map {
                    Message(
                        id = it[Messages.id],
                        chatId = it[Messages.chatId],
                        userId = it[Messages.userId],
                        message = it[Messages.message],
                        timestamp = it[Messages.timestamp],
                        repliedTo = it[Messages.repliedTo],
                    )
                }
            }
        }
    }

    internal fun contactsUpdated() {
        contactsFlow.update {
            transaction {
                Contacts.selectAll().map {
                    Contact(
                        userId = it[Contacts.userId],
                        contactId = it[Contacts.contactId]
                    )
                }
            }
        }
    }

    internal fun membersUpdated() {
        membersFlow.update {
            transaction {
                Members.selectAll().map {
                    Member(
                        chatId = it[Members.chatId],
                        userId = it[Members.userId],
                    )
                }
            }
        }
    }

    internal fun usersUpdated() {
        usersFlow.update {
            transaction {
                Users.selectAll().map {
                    User(
                        id = it[Users.id],
                        username = it[Users.username],
                        email = it[Users.email],
                        password = it[Users.password],
                        phone = it[Users.phone],
                    )
                }
            }
        }
    }

    internal fun getChats(): List<Chat> {
        return transaction {
            Chats.selectAll().map {
                Chat(
                    id = it[Chats.id],
                    name = it[Chats.name],
                    creatorId = it[Chats.creatorId],
                )
            }
        }
    }


    internal fun getMessages(): List<Message> {
        return transaction {
            Messages.selectAll().map {
                Message(
                    id = it[Messages.id],
                    chatId = it[Messages.chatId],
                    userId = it[Messages.userId],
                    message = it[Messages.message],
                    timestamp = it[Messages.timestamp],
                    repliedTo = it[Messages.repliedTo],
                )
            }
        }
    }

    internal fun getMembers(): List<Member> {
        return transaction {
            Members.selectAll().map {
                Member(
                    chatId = it[Members.chatId],
                    userId = it[Members.userId],
                )
            }
        }
    }

    internal fun getUsers(): List<User> {
        return transaction {
            Users.selectAll().map {
                User(
                    id = it[Users.id],
                    username = it[Users.username],
                    email = it[Users.email],
                    password = it[Users.password],
                    phone = it[Users.phone],
                )
            }
        }
    }

}
