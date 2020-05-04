package top.rules

import arrow.Kind
import arrow.core.nel
import top.User
import top.ValidationError
import top.ValidationError.UserCityInvalid
import top.ValidationError.UserLoginExits
import top.typeclass.EffectValidator

/**
 * ------------User Rules------------
 */
fun <F, S> EffectValidator<F, S, ValidationError>.cityShouldBeValid(user: User): Kind<F, Kind<S, Boolean>> = fx.async {
    repo.run {
        val cityValid: Boolean = user.isUserCityValid().bind()
        if (cityValid) validatorAE.just(cityValid)
        else validatorAE.raiseError(UserCityInvalid(user.city).nel())
    }
}

fun <F, S> EffectValidator<F, S, ValidationError>.loginShouldNotExit(user: User): Kind<F, Kind<S, Boolean>> = fx.async {
    repo.run {
        val loginExists: Boolean = user.doesUserLoginExist().bind()
        if (loginExists) validatorAE.raiseError(UserLoginExits(user.login).nel())
        else validatorAE.just(loginExists)
    }
}
