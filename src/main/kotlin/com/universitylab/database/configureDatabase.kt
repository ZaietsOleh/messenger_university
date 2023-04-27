package com.universitylab.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val database = Database.connect("jdbc:mysql://localhost:3306/messangerdb", driver = "com.mysql.cj.jdbc.Driver", user = "root", password = "mysql_password_80")
        transaction(database) {
             addLogger(StdOutSqlLogger)
             SchemaUtils.create(Chats, Messages, Members, Users, Contacts, Sessions)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
