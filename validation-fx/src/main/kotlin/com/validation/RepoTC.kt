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
    fun User.doesUserExist(): Kind<F, Boolean>
    fun User.doesUserCityExist(): Kind<F, Boolean>
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
                    if (!doesUserCityExist()) {
                        if (!doesUserExist()) {
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

    private fun User.isValidEmail(): Either<String, User> {
        return Rules.failFast<ValidationError>().run {
                    emailRuleRunner("nowhere.com")
                }.fix()
                .bimap(
                        { "$this@isValidEmail email validation error: ${it.head}" },
                        { this@isValidEmail }
                )
    }

}


