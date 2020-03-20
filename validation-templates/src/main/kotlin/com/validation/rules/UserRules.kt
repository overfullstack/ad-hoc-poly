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
private fun <F, S> EffectValidator<F, S, ValidationError>.cityShouldBeValid(user: User) = fx.async {
    repo.run {
        validatorAE.run {
            val cityValid = user.isUserCityValid().bind()
            if (cityValid) just(cityValid)
            else raiseError(UserCityInvalid(user.city).nel())
        }
    }
}

private fun <F, S> EffectValidator<F, S, ValidationError>.loginShouldNotExit(user: User) = fx.async {
    repo.run {
        validatorAE.run {
            val loginExists = user.doesUserLoginExist().bind()
            if (loginExists) raiseError(UserLoginExits(user.login).nel())
            else just(loginExists)
        }
    }
}

fun <F, S> EffectValidator<F, S, ValidationError>.validateUserWithRules(user: User) = fx.async {
    validatorAE.run {
        mapN( // 🚩 All these methods are called, even in Fail-Fast mode. No better way to do it as of now.
                validateEmailWithRules(user.email),
                cityShouldBeValid(user).bind(),
                loginShouldNotExit(user).bind()
        ) {}.handleErrorWith { raiseError(it) }
    }
}



