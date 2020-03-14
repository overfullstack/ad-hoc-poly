package com.validation

import arrow.Kind
import arrow.core.nel
import arrow.fx.typeclasses.Async
import com.validation.ValidationError.UserCityInvalid
import com.validation.ValidationError.UserLoginExits

interface RepoTC<F> : Async<F> {
    fun User.doesUserLoginExist(): Kind<F, Boolean>
    fun User.isUserCityValid(): Kind<F, Boolean>
    fun User.update(): Kind<F, Unit>
    fun User.insert(): Kind<F, Unit>

    /**
     * ------------User Rules------------
     */
    fun <S> RuleRunnerStrategy<S, ValidationError>.userCityShouldBeValid(user: User) = fx.async {
        val cityValid = user.isUserCityValid().bind()
        if (cityValid) this@userCityShouldBeValid.just(cityValid)
        else raiseError(UserCityInvalid(user.city).nel())
    }

    fun <S> RuleRunnerStrategy<S, ValidationError>.userLoginShouldNotExit(user: User) = fx.async {
        val userExists = user.doesUserLoginExist().bind()
        if (userExists) raiseError(UserLoginExits(user.login).nel())
        else this@userLoginShouldNotExit.just(userExists)
    }

    fun <S> RuleRunnerStrategy<S, ValidationError>.userRuleRunner(user: User) = fx.async {
        mapN(
                emailRuleRunner(user.email),
                !userCityShouldBeValid(user),
                !userLoginShouldNotExit(user)
        ) {
            user
        }.handleErrorWith { raiseError(it) }
    }

}


