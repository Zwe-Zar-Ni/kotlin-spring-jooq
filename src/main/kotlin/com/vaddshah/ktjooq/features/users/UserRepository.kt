package com.vaddshah.ktjooq.features.users

import com.vaddshah.ktjooq.features.users.dtos.User
import com.vaddshah.ktjooq.features.users.dtos.UserResponse
import com.vaddshah.ktjooq.generated.tables.records.UsersRecord
import com.vaddshah.ktjooq.generated.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class UserRepository(private val dsl: DSLContext) {

    fun findAll(): List<UserResponse> =
        dsl.selectFrom(USERS)
            .orderBy(USERS.ID.desc())
            .fetch { toUserResponse(it) }

    @Transactional
    fun create(request: User): UserResponse {
        val record = dsl.insertInto(USERS)
            .set(USERS.NAME, request.name)
            .set(USERS.EMAIL, request.email)
            .set(USERS.PASSWORD, request.password)
            .returning()
            .fetchOne()
        val user = toUserResponse(record ?: error("Insert returned no row"))
        return user
    }

    fun findByEmail(email: String): User? {
        val user = dsl.selectFrom(USERS).where(USERS.EMAIL.eq(email)).fetchOne() ?: return null
        return User(
            id = requireNotNull(user.id),
            name = requireNotNull(user.name),
            email = requireNotNull(user.email),
            password = requireNotNull(user.password),
            createdAt = requireNotNull(user.createdAt),
            updatedAt = requireNotNull(user.updatedAt),
        )
    }

    private fun toUserResponse(record: UsersRecord): UserResponse = UserResponse(
        id = requireNotNull(record.id),
        name = requireNotNull(record.name),
        email = requireNotNull(record.email),
        createdAt = requireNotNull(record.createdAt),
        updatedAt = requireNotNull(record.updatedAt),
    )
}