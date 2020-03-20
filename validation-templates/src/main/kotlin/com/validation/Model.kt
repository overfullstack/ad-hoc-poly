package com.validation

import org.springframework.data.annotation.Id

data class User(
        @Id val login: String,
        val email: String,
        val firstName: String,
        val lastName: String,
        val city: String
)

data class City(
        @Id val name: String
)

sealed class ValidationError(val msg: String) {
    data class DoesNotContain(val value: String) : ValidationError("Did not contain $value")
    data class EmailMaxLength(val value: Int) : ValidationError("Exceeded length of $value")

    data class UserLoginExits(val login: String) : ValidationError("User exists with Login - $login")
    data class UserCityInvalid(val city: String) : ValidationError("Invalid User city - $city")
}
