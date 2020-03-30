package com.sample

import arrow.Kind
import arrow.Kind2
import arrow.core.Either
import arrow.core.ForEither
import arrow.core.NonEmptyList
import arrow.core.extensions.either.bifunctor.bifunctor
import arrow.core.fix
import arrow.fx.ForIO
import arrow.fx.fix
import com.validation.User
import com.validation.ValidationError
import com.validation.rules.validateUserWithRules
import com.validation.typeclass.EffectValidator
import com.validation.typeclass.ForFailFast
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.ServerResponse.badRequest
import org.springframework.web.servlet.function.ServerResponse.ok
import org.springframework.web.servlet.function.body

class HandlersX(private val blockingValidator: EffectValidator<ForIO, ForFailFast<ValidationError>, ValidationError>) {
    fun upsertX(request: ServerRequest): ServerResponse {
        val user = request.body<User>()
        return blockingValidator.run {
            val result: Kind2<ForEither, NonEmptyList<ValidationError>, Unit> = validateUserWithRules(user).fix().unsafeRunSync()
            repo.run {
                user.upsert(Either.bifunctor(), result).fix()
            }.fold(
                    { it.fold(badRequest()::body, ok()::body) },
                    ok()::body
            )
        }
    }
}
