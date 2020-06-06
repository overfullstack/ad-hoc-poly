package mvc

import arrow.core.*
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.ServerResponse.badRequest
import org.springframework.web.servlet.function.ServerResponse.ok
import org.springframework.web.servlet.function.body
import top.User
import top.ValidationError
import top.typeclass.errorAccumulation
import top.typeclass.failFast
import top.typeclass.validateEmailWithRules

class Handlers(
        private val userRepository: UserRepository,
        private val cityRepository: CityRepository
) {
    fun listApi(request: ServerRequest): ServerResponse {
        return ok().contentType(MediaType.APPLICATION_JSON).body(userRepository.findAll())
    }

    fun upsert(request: ServerRequest): ServerResponse { // 👎🏼 This is struck with using FailFast strategy
        val user = request.body<User>()
        val isEmailValid: Either<ValidationError, Unit> = validateEmailFailFast(user.email)
        return isEmailValid.fold(
                { badRequest().body("$user email validation error: $it") },
                {
                    if (cityRepository.doesCityExistsWith(user.city)) {
                        if (userRepository.doesUserExitsWith(user.login)) {
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

        private fun validateEmailFailFastX(email: String): Either<NonEmptyList<ValidationError>, String> =
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

        private fun validateEmailErrorAccumulationX(email: String): Validated<NonEmptyList<ValidationError>, Unit> =
                errorAccumulation<ValidationError>().run {
                    validateEmailWithRules(email).fix()
                }
    }
}
