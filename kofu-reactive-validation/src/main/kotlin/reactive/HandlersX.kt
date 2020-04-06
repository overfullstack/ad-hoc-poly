package reactive

import arrow.core.Validated
import arrow.core.extensions.validated.bifunctor.bifunctor
import arrow.core.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import top.User
import top.ValidationError
import top.rules.validateUserWithRules
import top.typeclass.EffectValidator
import top.typeclass.ForErrorAccumulation

class HandlersX(private val nonBlockingReactorEAValidator: EffectValidator<ForMonoK,
        ForErrorAccumulation<ValidationError>, ValidationError>) {
    fun upsertX(request: ServerRequest): Mono<ServerResponse> =
            request.bodyToMono<User>()
                    .flatMap { user ->
                        nonBlockingReactorEAValidator.run {
                            validateUserWithRules(user).fix().mono
                                    .flatMap {
                                        repo.run {
                                            user.upsert(Validated.bifunctor(), it).fix()
                                        }.fold(
                                                { it.fold(badRequest()::bodyValue, ok()::bodyValue) },
                                                ok()::bodyValue
                                        )
                                    }
                        }
                    }
}
