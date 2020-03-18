package com.sample

import arrow.core.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import com.validation.User
import com.validation.ValidationError
import com.validation.rules.validateUserWithRules
import com.validation.typeclass.EffectValidator
import com.validation.typeclass.ForErrorAccumulation
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono

class HandlersX(private val nonBlockingReactorEAValidator: EffectValidator<ForMonoK, ForErrorAccumulation<ValidationError>, ValidationError>) {
    fun upsertX(request: ServerRequest) =
            request.bodyToMono<User>()
                    .flatMap { user ->
                        nonBlockingReactorEAValidator.run {
                            validateUserWithRules(user).fix().mono
                                    .map {
                                        repo.run {
                                            // Migrate it to use `upsert` when `ValidatedBiFunctor` is introduced
                                            it.fix().bimap(user.toLeft(), user.toRight())
                                        }
                                    }
                                    .flatMap { result ->
                                        result.fold(
                                                { it.fold(badRequest()::bodyValue, ok()::bodyValue) },
                                                ok()::bodyValue
                                        )
                                    }
                        }
                    }
}
