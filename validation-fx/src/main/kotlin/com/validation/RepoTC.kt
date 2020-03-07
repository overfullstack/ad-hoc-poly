package com.validation

import arrow.Kind
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import arrow.fx.reactor.k
import arrow.fx.typeclasses.Async
import reactor.core.publisher.Mono

fun <R, F> Async<F>.forMono(thunk: suspend () -> Mono<R>) = effect { thunk().k().suspended() }
fun <R, F> Async<F>.forIO(thunk: suspend () -> R) = effect { thunk() }

interface RepoTC<F> : Async<F> {
    fun User.get(): Kind<F, User?>
    fun User.doesUserExistWith(): Kind<F, Boolean>
    fun User.doesCityExistWith(): Kind<F, Boolean>
    fun User.update(): Kind<F, Any?>
    fun User.insert(): Kind<F, Any?>

    fun User.validateUserForRegister(): Kind<F, Either<String, User>> = fx.async {
        val user = !get().handleError { null } // null indicating user doesn't exist
        Either.fx<String, User> {
            val isLoginExists = if (user != null) "$user exists".left() else Unit.right()
            !isLoginExists
            !this@validateUserForRegister.isValidEmail()
        }
    }

    fun User.validateUserForUpsert(): Kind<F, Either<String, String>> = fx.async {
        this@validateUserForUpsert.isValidEmail().fold(
                { it.left() as Either<String, String> },
                {
                    if (!doesCityExistWith()) {
                        if (!doesUserExistWith()) {
                            it.update()
                            "Updated!! $it".right()
                        } else {
                            it.insert()
                            "Created!! $it".right()
                        }
                    } else {
                        "City is invalid!! : $it".left()
                    }
                }
        )
    }

    private fun User.isValidEmail() = Either.fx<String, User> {
        val isEmailValid = Rules failFast {
            Email(email).validateEmail().fix()
        }
        !isEmailValid.bimap(
                { "$this@isValidEmail email validation error: ${it.head}" },
                { this@isValidEmail }
        )
    }
}


