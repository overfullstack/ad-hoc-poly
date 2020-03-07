package com.sample

import arrow.core.fix
import arrow.core.left
import arrow.core.right
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

    fun register(request: ServerRequest) =
            request.bodyToMono<User>()
                    .flatMap { user ->
                        userRepository.findOne(user.login)
                                .map { existingUser -> if (existingUser != null) Unit.left() else Unit.right() }
                                .flatMap { isLoginExists ->
                                    isLoginExists.fold(
                                            { badRequest().bodyValue("User with same login exists!!") },
                                            {
                                                val isEmailValid = Rules.failFast<ValidationError>().run {
                                                    emailRuleRunner(user.email)
                                                }.fix()
                                                isEmailValid.fold(
                                                        { badRequest().bodyValue("$user email validation error: ${it.head}") },
                                                        {
                                                            userRepository.save(user)
                                                            ok().bodyValue("Created!! $user")
                                                        }
                                                )
                                            }
                                    )
                                }
                    }


    fun registerX(request: ServerRequest) =
            request.bodyToMono<User>()
                    .flatMap { user ->
                        nonBlockingReactorRepo.run {
                            user.validateUserForRegister().fix().mono
                        }
                    }.flatMap { validationResult ->
                        validationResult.fold(
                                { badRequest().bodyValue(it) },
                                {
                                    userRepository.save(it)
                                    ok().bodyValue("Created!! $it")
                                }
                        )
                    }

    fun upsert(request: ServerRequest) =
            request.bodyToMono<User>()
                    .flatMap { user ->
                        val isEmailValid = Rules.failFast<ValidationError>().run { 
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
                                                                    ok().bodyValue("Created!! $user")
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
                            user.validateUserForUpsert().fix().mono
                        }
                    }.flatMap { validationResult ->
                        validationResult.fold(
                                { badRequest().bodyValue(it) },
                                { ok().bodyValue(it) }
                        )
                    }

    fun listApi(request: ServerRequest) =
            ok().contentType(MediaType.APPLICATION_JSON)
                    .body(userRepository.findAll())
}
