package com.bs.basicktorserver.exposed

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", length = 50)
    val email = varchar("email", length = 100)

    override val primaryKey = PrimaryKey(id)
}