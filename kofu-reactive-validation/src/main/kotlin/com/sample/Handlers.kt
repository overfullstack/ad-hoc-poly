package com.sample

import arrow.core.Either
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import com.validation.RepoTC
import com.validation.RuleRunnerStrategy
import com.validation.User
import com.validation.ValidationError
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
            ok().contentType(MediaType.APPLICATION_JSON).body(userRepository.findAll())

    fun upsert(request: ServerRequest) = // 👎🏼 This is struck with using FailFast strategy 
            request.bodyToMono<User>()
                    .flatMap { user ->
                        val isEmailValid = validateEmail(user.email)
                        isEmailValid.fold(
                                { badRequest().bodyValue("$user email validation errors: $it") },
                                {
                                    cityRepository.findFirstCityWith(user.city)
                                            .flatMap { cityExists ->
                                                if (cityExists) {
                                                    userRepository.findFirstUserWith(user.login)
                                                            .flatMap { userExists ->
                                                                if (userExists) {
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

    companion object Utils {
        // 📝 Note: This logic is readily reusable by both services, as it has no effect association.
        private fun validateEmail(email: String): Either<ValidationError, String> =
                if (email.contains("@", false)) {
                    if (email.length <= 250) {
                        email.right()
                    } else {
                        ValidationError.MaxLength(250).left()
                    }
                } else {
                    ValidationError.DoesNotContain("@").left()
                }
    }

    fun upsertX(request: ServerRequest) =
            request.bodyToMono<User>()
                    .flatMap { user ->
                        nonBlockingReactorRepo.run {
                            RuleRunnerStrategy.ErrorAccumulationStrategy<ValidationError>().run {
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
