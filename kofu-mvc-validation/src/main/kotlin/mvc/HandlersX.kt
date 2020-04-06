package mvc

import arrow.Kind2
import arrow.core.Either
import arrow.core.ForEither
import arrow.core.NonEmptyList
import arrow.core.extensions.either.bifunctor.bifunctor
import arrow.core.fix
import arrow.fx.ForIO
import arrow.fx.fix
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.ServerResponse.badRequest
import org.springframework.web.servlet.function.ServerResponse.ok
import org.springframework.web.servlet.function.body
import top.User
import top.ValidationError
import top.rules.validateUserWithRules
import top.typeclass.EffectValidator
import top.typeclass.ForFailFast

class HandlersX(private val blockingFFValidator: EffectValidator<ForIO, ForFailFast<ValidationError>, ValidationError>) {
    fun upsertX(request: ServerRequest): ServerResponse {
        val user = request.body<User>()
        return blockingFFValidator.run {
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
