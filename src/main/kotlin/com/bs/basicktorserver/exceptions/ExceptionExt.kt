package com.bs.basicktorserver.exceptions

import org.jetbrains.exposed.exceptions.ExposedSQLException

fun ExposedSQLException.isUniqueConstraintViolation(): Boolean {
    val msg = message?.lowercase() ?: ""
    return msg.contains("duplicate") || msg.contains("unique") || msg.contains("constraint")
}