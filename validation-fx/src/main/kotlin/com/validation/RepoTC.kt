package com.validation

import arrow.Kind
import arrow.core.nel
import arrow.fx.typeclasses.Async
import com.validation.ValidationError.UserCityInvalid
import com.validation.ValidationError.UserLoginExits

interface RepoTC<F, S> {
    val effect: Async<F>
    val strategy: RuleRunnerStrategy<S, ValidationError>
    
    fun User.doesUserLoginExist(): Kind<F, Boolean>
    fun User.isUserCityValid(): Kind<F, Boolean>
    fun User.update(): Kind<F, Unit>
    fun User.insert(): Kind<F, Unit>

    /**
     * ------------User Rules------------
     */
    private fun User.userCityShouldBeValid() = effect.fx.async {
        val cityValid = isUserCityValid().bind()
        if (cityValid) strategy.just(cityValid)
        else strategy.raiseError(UserCityInvalid(city).nel())
    }

    private fun User.userLoginShouldNotExit() = effect.fx.async {
        val userExists = doesUserLoginExist().bind()
        if (userExists) strategy.raiseError(UserLoginExits(login).nel())
        else strategy.just(userExists)
    }

    fun User.userRuleRunner() = effect.fx.async {
        strategy.run {
            mapN(
                    emailRuleRunner(email),
                    !userCityShouldBeValid(),
                    !userLoginShouldNotExit()
            ) {}.handleErrorWith { raiseError(it) }
        }
    }

}


