package com.sample

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.validation.User
import com.validation.ValidationError
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.ServerResponse.badRequest
import org.springframework.web.servlet.function.ServerResponse.ok
import org.springframework.web.servlet.function.body

class Handlers(
        private val userRepository: UserRepository,
        private val cityRepository: CityRepository
) {
    fun listApi(request: ServerRequest): ServerResponse {
        return ok().contentType(MediaType.APPLICATION_JSON).body(userRepository.findAll())
    }

    fun upsert(request: ServerRequest): ServerResponse { // 👎🏼 This is struck with using FailFast strategy
        val user = request.body<User>()
        val isEmailValid = validateEmail(user.email)
        return isEmailValid.fold(
                { badRequest().body("$user email validation error: $it") },
                {
                    if (cityRepository.findFirstCityWith(user.city)) {
                        if (userRepository.findFirstUserWith(user.login)) {
                            userRepository.update(user)
                            ok().body("Updated!! $user")
                        } else {
                            userRepository.insert(user)
                            ok().body("Inserted!! $user")
                        }
                    } else {
                        badRequest().body("City is invalid!! : $user")
                    }
                }
        )
    }

    companion object Utils {
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
}
