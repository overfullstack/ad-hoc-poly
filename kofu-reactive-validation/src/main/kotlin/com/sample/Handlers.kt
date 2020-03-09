package com.sample

import arrow.core.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import com.validation.*
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono

@Suppress("UNUSED_PARAMETER")
class UserHandler(
        private val userRepository: UserRepository,
        private val cityRepository: CityRepository,
        private val nonBlockingReactorRepo: RepoTC<ForMonoK>
) {
    fun listApi(request: ServerRequest) =
            ok().contentType(MediaType.APPLICATION_JSON)
                    .body(userRepository.findAll())

    fun upsert(request: ServerRequest) =
            request.bodyToMono<User>()
                    .flatMap { user ->
                        val isEmailValid = RulesRunnerStrategy.failFast<ValidationError>().run {
                            emailRuleRunner(user.email)
                        }.fix()
                        isEmailValid.fold(
                                { badRequest().bodyValue("$user email validation error: ${it.head}") },
                                {
                                    cityRepository.doesCityExistWith(user.city)
                                            .flatMap { cityExists ->
                                                if (cityExists) {
                                                    userRepository.doesUserExistWith(user.login)
                                                            .flatMap { userExists ->
                                                                if (userExists) {
                                                                    userRepository.update(user)
                                                                    ok().bodyValue("Updated!! $user")
                                                                } else {
                                                                    userRepository.save(user)
                                                                    ok().bodyValue("Inserted!! $user")
                                                                }
                                                            }
                                                } else {
                                                    badRequest().bodyValue("City is invalid!! : $user")
                                                }
                                            }
                                }
                        )
                    }

    fun upsertX(request: ServerRequest) =
            request.bodyToMono<User>()
                    .flatMap { user ->
                        nonBlockingReactorRepo.run {
                            RulesRunnerStrategy.failFast<ValidationError>().run {
                                validateForUpsert(user).fix().mono
                            }
                        }
                    }.flatMap { validationResult ->
                        validationResult.fold(
                                { badRequest().bodyValue(it) },
                                { ok().bodyValue(it) }
                        )
                    }

}
