package com.validation

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
