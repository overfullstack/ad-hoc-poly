package mvc

import arrow.core.Either
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
import top.typeclass.EffectValidatorFailFast
import top.typeclass.validateUserWithRules

class HandlersX(private val blockingFFValidator: EffectValidatorFailFast<ForIO, ValidationError>) {
    fun upsertX(request: ServerRequest): ServerResponse {
        val user = request.body<User>()
        return blockingFFValidator.run {
            val result = validateUserWithRules(user).fix().unsafeRunSync().fix()
            repo.run {
                user.upsert(Either.bifunctor(), result).fix()
            }.fold(
                    { it.fold(badRequest()::body, ok()::body) },
                    ok()::body
            )
        }
    }
}
