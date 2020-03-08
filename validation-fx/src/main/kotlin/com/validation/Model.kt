package com.validation

import arrow.core.Nel

data class User(
        val login: String,
        val email: String,
        val firstname: String,
        val lastname: String,
        val city: String
)

data class City(
        val name: String
)

data class Email(val value: String)

sealed class ValidationError(val msg: String) {
    data class DoesNotContain(val value: String) : ValidationError("Did not contain $value")
    data class MaxLength(val value: Int) : ValidationError("Exceeded length of $value")
    data class NotAnEmail(val reasons: Nel<ValidationError>) : ValidationError("Not a valid email")
    
    data class UserLoginExits(val login: String): ValidationError("User exists with Login - $login")
    data class UserCityInvalid(val city: String): ValidationError("Invalid User city - $city")
    data class InvalidUser(val reasons: Nel<ValidationError>) : ValidationError("Not a valid user")
}
