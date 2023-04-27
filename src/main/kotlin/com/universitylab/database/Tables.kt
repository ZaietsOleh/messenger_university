package com.universitylab.database

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
internal data class Chat(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("creatorId") val creatorId: Int,
)

object Chats : Table("Chats") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val creatorId = integer("creatorId")
    override val primaryKey = PrimaryKey(id, name = "PK_Chat_Id")
}

@Serializable
internal data class Message(
    @SerialName("id") val id: Int,
    @SerialName("chatId") val chatId: Int,
    @SerialName("userId") val userId: Int,
    @SerialName("message") val message: String,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("repliedTo") val repliedTo: Int? = null,
)

object Messages : Table("Messages") {
    val id = integer("id").autoIncrement()
    val chatId = integer("chatId")
    val userId = integer("userId")
    val message = varchar("message", 255)
    val timestamp = long("timestamp")
    val repliedTo = integer("repliedTo").nullable()
    override val primaryKey = PrimaryKey(id, name = "PK_Message_Id")
}

@Serializable
internal data class Member(
    @SerialName("chatId") val chatId: Int,
    @SerialName("userId") val userId: Int,
)

object Members : Table("Members") {
    val chatId = integer("chatId")
    val userId = integer("userId")
}

@Serializable
data class User(
    @SerialName("id") val id: Int,
    @SerialName("username") val username: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("phone") val phone: String,
)

object Users : Table("Users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 255)
    val email = varchar("email", 255)
    val password = varchar("password", 255)
    val phone = varchar("phone", 255)
    override val primaryKey = PrimaryKey(id, name = "PK_User_Id")
}

@Serializable
internal data class Contact(
    @SerialName("userId") val userId: Int,
    @SerialName("contactId") val contactId: Int,
)

object Contacts : Table("Contacts") {
    val userId = integer("userId")
    val contactId = integer("contactId")
}

@Serializable
internal data class UserSessionInfo(
    @SerialName("userId") val userId: Int,
    @SerialName("token") val token: String,
)

object Sessions : Table("Sessions") {
    val userId = integer("userId")
    val token = varchar("token", 255)
}
