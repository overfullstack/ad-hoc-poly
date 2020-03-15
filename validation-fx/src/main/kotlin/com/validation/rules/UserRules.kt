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
private fun <F, S> EffectValidator<F, S, ValidationError>.userCityShouldBeValid(user: User) = repo.run {
    fx.async {
        val cityValid = user.isUserCityValid().bind()
        if (cityValid) validator.just(cityValid)
        else validator.raiseError(UserCityInvalid(user.city).nel())
    }
}

private fun <F, S> EffectValidator<F, S, ValidationError>.userLoginShouldNotExit(user: User) = repo.run {
    fx.async {
        val userExists = user.doesUserLoginExist().bind()
        if (userExists) validator.raiseError(UserLoginExits(user.login).nel())
        else validator.just(userExists)
    }
}

fun <F, S> EffectValidator<F, S, ValidationError>.validateWithRules(user: User) = repo.run {
    fx.async {
        validator.run {
            mapN(
                    validateEmailWithRules(user.email),
                    !userCityShouldBeValid(user),
                    !userLoginShouldNotExit(user)
            ) {}.handleErrorWith { raiseError(it) }
        }
    }
}



