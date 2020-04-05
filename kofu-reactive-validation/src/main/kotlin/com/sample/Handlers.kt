package com.sample

import arrow.core.*
import com.validation.User
import com.validation.ValidationError
import com.validation.rules.validateEmailWithRules
import com.validation.typeclass.errorAccumulation
import com.validation.typeclass.failFast
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

class Handlers(
    private val userRepository: UserRepository,
    private val cityRepository: CityRepository
) {
    fun listApi(request: ServerRequest) =
            ok().contentType(MediaType.APPLICATION_JSON).body(userRepository.findAll())

    fun upsert(request: ServerRequest): Mono<ServerResponse> = // 👎🏼 This is struck with using FailFast strategy 
            request.bodyToMono<User>()
                    .flatMap { user ->
                        val isEmailValid: Either<ValidationError, Unit> = validateEmailFailFast(user.email)
                        isEmailValid.fold(
                                { badRequest().bodyValue("$user email validation errors: $it") },
                                {
                                    cityRepository.doesCityExistsWith(user.city)
                                            .flatMap { cityExists ->
                                                if (cityExists) {
                                                    userRepository.doesUserExistsWith(user.login)
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
        private fun validateEmailFailFast(email: String): Either<ValidationError, Unit> =
                if (email.contains("@", false)) {
                    if (email.length <= 250) {
                        Unit.right()
                    } else {
                        ValidationError.EmailMaxLength(250).left()
                    }
                } else {
                    ValidationError.DoesNotContain("@").left()
                }

        private fun validateEmailFailFastX(email: String): Either<NonEmptyList<ValidationError>, Tuple2<String, String>> =
                failFast<ValidationError>().run {
                    validateEmailWithRules(email).fix()
                }

        private fun validateEmailErrorAccumulation(email: String): Either<MutableList<ValidationError>, Unit> {
            val errorList = mutableListOf<ValidationError>()
            if (!email.contains("@", false)) {
                errorList.add(ValidationError.DoesNotContain("@"))
            }
            if (email.length > 250) {
                errorList.add(ValidationError.EmailMaxLength(250))
            }
            return if (errorList.isNotEmpty()) errorList.left() else Unit.right()
        }

        private fun validateEmailErrorAccumulationX(email: String): Validated<NonEmptyList<ValidationError>, Tuple2<String, String>> =
                errorAccumulation<ValidationError>().run {
                    validateEmailWithRules(email).fix()
                }

    }
}
