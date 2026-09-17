package com.vaddshah.ktjooq.features.users

import com.vaddshah.ktjooq.features.users.dtos.CreateUserRequest
import com.vaddshah.ktjooq.features.users.dtos.User
import com.vaddshah.ktjooq.generated.tables.records.UsersRecord
import com.vaddshah.ktjooq.generated.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class UserRepository(private val dsl: DSLContext) {
    fun findAll(): List<User> =
        dsl.selectFrom(USERS)
            .orderBy(USERS.ID.desc())
            .fetch { toUser(it) }

    @Transactional
    fun create(request: CreateUserRequest): User {
        val record = dsl.insertInto(USERS)
            .set(USERS.NAME, request.name)
            .set(USERS.EMAIL, request.email)
            .set(USERS.PASSWORD, request.password)
            .returning()
            .fetchOne()
        return toUser(record ?: error("Insert returned no row"))
    }

    private fun toUser(record: UsersRecord): User = User(
        id = requireNotNull(record.id),
        name = requireNotNull(record.name),
        email = requireNotNull(record.email),
        createdAt = requireNotNull(record.createdAt),
        updatedAt = requireNotNull(record.updatedAt),
    )
}