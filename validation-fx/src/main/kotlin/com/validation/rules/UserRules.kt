package com.validation.rules

import arrow.core.nel
import com.validation.typeclass.Repo
import com.validation.User
import com.validation.ValidationError.UserCityInvalid
import com.validation.ValidationError.UserLoginExits

/**
 * ------------User Rules------------
 */
private fun <F, S> Repo<F, S>.userCityShouldBeValid(user: User) = effect.fx.async {
    val cityValid = user.isUserCityValid().bind()
    if (cityValid) ruleRunStrategy.just(cityValid)
    else ruleRunStrategy.raiseError(UserCityInvalid(user.city).nel())
}

private fun <F, S> Repo<F, S>.userLoginShouldNotExit(user: User) = effect.fx.async {
    val userExists = user.doesUserLoginExist().bind()
    if (userExists) ruleRunStrategy.raiseError(UserLoginExits(user.login).nel())
    else ruleRunStrategy.just(userExists)
}

fun <F, S> Repo<F, S>.validateWithRules(user: User) = effect.fx.async {
    ruleRunStrategy.run {
        mapN(
                validateEmailWithRules(user.email),
                !userCityShouldBeValid(user),
                !userLoginShouldNotExit(user)
        ) {}.handleErrorWith { raiseError(it) }
    }
}



