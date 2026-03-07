package com.bs.basicktorserver.data.models

import org.jetbrains.exposed.sql.Table

object Notes : Table() {
    val id = integer("id").autoIncrement()
    // Here is our Foreign Key linking back to the Users table!
    val userId = integer("user_id").references(Users.id)
    val title = varchar("title", length = 255)
    val content = varchar("content", length = 1000)

    override val primaryKey = PrimaryKey(id)
}