package com.bs.basicktorserver.data.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.CurrentDateTime

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", length = 50).uniqueIndex()
    val email = varchar("email", length = 100)
    val password = varchar("password", length = 200)
    val createdAt = datetime("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}