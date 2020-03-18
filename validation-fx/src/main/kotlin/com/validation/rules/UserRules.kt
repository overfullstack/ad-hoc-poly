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
private fun <F, S> EffectValidator<F, S, ValidationError>.cityShouldBeValid(user: User) = repo.run {
    fx.async {
        validatorAE.run {
            val cityValid = user.isUserCityValid().bind()
            if (cityValid) just(cityValid)
            else raiseError(UserCityInvalid(user.city).nel())
        }
    }
}

private fun <F, S> EffectValidator<F, S, ValidationError>.loginShouldNotExit(user: User) = repo.run {
    fx.async {
        validatorAE.run {
            val loginExists = user.doesUserLoginExist().bind()
            if (loginExists) raiseError(UserLoginExits(user.login).nel())
            else just(loginExists)
        }
    }
}

fun <F, S> EffectValidator<F, S, ValidationError>.validateUserWithRules(user: User) = repo.run {
    fx.async {
        validatorAE.run {
            mapN( // 🚩 This has a bug, order of validation is not from left to right. Waiting for bug fix.
                    validateEmailWithRules(user.email),
                    cityShouldBeValid(user).bind(),
                    loginShouldNotExit(user).bind()
            ) {}.handleErrorWith { raiseError(it) }
        }
    }
}



