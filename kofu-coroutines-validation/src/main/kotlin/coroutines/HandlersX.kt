package coroutines

import arrow.Kind2
import arrow.core.Either
import arrow.core.ForEither
import arrow.core.NonEmptyList
import arrow.core.extensions.either.bifunctor.bifunctor
import arrow.core.fix
import arrow.fx.ForIO
import arrow.fx.fix
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import top.User
import top.ValidationError
import top.typeclass.EffectValidator
import top.typeclass.ForFailFast

class HandlersX(private val coroutineFFValidator: EffectValidator<ForIO, ForFailFast<ValidationError>, ValidationError>) {
    suspend fun upsertX(request: ServerRequest): ServerResponse {
        val user = request.awaitBody<User>()
        return coroutineFFValidator.run {
            val result: Kind2<ForEither, NonEmptyList<ValidationError>, Unit> = validateUserWithRules(user).fix().suspended()
            repo.run {
                user.upsert(Either.bifunctor(), result).fix()
            }.fold(
                    { error -> error.fold({ badRequest().bodyValueAndAwait(it) }, { ok().bodyValueAndAwait(it) }) },
                    { ok().bodyValueAndAwait(it) }
            )
        }
    }
}
