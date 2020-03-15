package com.validation.rules

import arrow.core.nel
import com.validation.User
import com.validation.ValidationError
import com.validation.ValidationError.UserCityInvalid
import com.validation.ValidationError.UserLoginExits
import com.validation.typeclass.EffectValidator

/**
 * ------------User Rules------------
 */
private fun <F, S> EffectValidator<F, S, ValidationError>.userCityShouldBeValid(user: User) = repo.fx.async {
    val cityValid = user.isUserCityValid().bind()
    if (cityValid) simpleValidator.just(cityValid)
    else simpleValidator.raiseError(UserCityInvalid(user.city).nel())
}

private fun <F, S> EffectValidator<F, S, ValidationError>.userLoginShouldNotExit(user: User) = repo.fx.async {
    val userExists = user.doesUserLoginExist().bind()
    if (userExists) simpleValidator.raiseError(UserLoginExits(user.login).nel())
    else simpleValidator.just(userExists)
}

fun <F, S> EffectValidator<F, S, ValidationError>.validateWithRules(user: User) = repo.fx.async {
    simpleValidator.run {
        mapN(
                validateEmailWithRules(user.email),
                !userCityShouldBeValid(user),
                !userLoginShouldNotExit(user)
        ) {}.handleErrorWith { raiseError(it) }
    }
}



