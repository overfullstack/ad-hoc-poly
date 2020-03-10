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
                                    cityRepository.findFirstCityWith(user.city)
                                            .flatMap { cityExists ->
                                                if (cityExists != 0) {
                                                    userRepository.findFirstUserWith(user.login)
                                                            .flatMap { userExists ->
                                                                if (userExists != 0) {
                                                                    userRepository.update(user)
                                                                    ok().bodyValue("Updated!! $user")
                                                                } else {
                                                                    userRepository.insert(user)
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
                            RulesRunnerStrategy.accumulateErrors<ValidationError>().run {
                                userRuleRunner(user).fix().mono
                            }
                        }.flatMap {
                            it.fix().fold(
                                    { reasons ->
                                        when (reasons.head) {
                                            ValidationError.UserLoginExits(user.login) -> {
                                                userRepository.update(user)
                                                ok().bodyValue("Updated!! $user")
                                            }
                                            else -> badRequest().bodyValue("Cannot Upsert!!, reasons: $reasons")
                                        }
                                    },
                                    {
                                        userRepository.insert(user)
                                        ok().bodyValue("Inserted!! $user")
                                    }
                            )
                        }
                    }

}
