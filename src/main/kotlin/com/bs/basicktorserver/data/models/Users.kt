package com.bs.basicktorserver.data.models

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", length = 50).uniqueIndex()
    val email = varchar("email", length = 100)
    val password = varchar("password", length = 200)

    override val primaryKey = PrimaryKey(id)
}