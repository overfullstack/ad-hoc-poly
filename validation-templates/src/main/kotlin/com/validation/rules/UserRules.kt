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
        val cityValid = user.isUserCityValid().bind()
        if (cityValid) validatorAE.just(cityValid)
        else validatorAE.raiseError(UserCityInvalid(user.city).nel())
    }
}

private fun <F, S> EffectValidator<F, S, ValidationError>.loginShouldNotExit(user: User) = fx.async {
    repo.run {
        val loginExists = user.doesUserLoginExist().bind()
        if (loginExists) validatorAE.raiseError(UserLoginExits(user.login).nel())
        else validatorAE.just(loginExists)
    }
}

fun <F, S> EffectValidator<F, S, ValidationError>.validateUserWithRules(user: User) = fx.async {
    validatorAE.run {
        // 🚩 These are eager calls. So even in fail-fast mode, where `Either` is used
        // all these methods are called even after first `raiseError` 
        // as `Either` doesn't have that short-circuit functionality.
        mapN(  
                validateEmailWithRules(user.email),
                cityShouldBeValid(user).bind(),
                loginShouldNotExit(user).bind()
        ) {}.handleErrorWith { raiseError(it) }
    }
}



