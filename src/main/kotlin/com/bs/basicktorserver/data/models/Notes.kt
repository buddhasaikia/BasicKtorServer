package com.bs.basicktorserver.data.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.CurrentDateTime

object Notes : Table() {
    val id = integer("id").autoIncrement()
    // Here is our Foreign Key linking back to the Users table!
    val userId = integer("user_id").references(Users.id)
    val title = varchar("title", length = 255)
    val content = varchar("content", length = 1000)
    val createdAt = datetime("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}