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
private fun <F, S> EffectValidator<F, S, ValidationError>.cityShouldBeValid(user: User): Kind<F, Kind<S, Boolean>> = fx.async {
    repo.run {
        val cityValid = user.isUserCityValid().bind()
        if (cityValid) validatorAE.just(cityValid)
        else validatorAE.raiseError(UserCityInvalid(user.city).nel())
    }
}

private fun <F, S> EffectValidator<F, S, ValidationError>.loginShouldNotExit(user: User): Kind<F, Kind<S, Boolean>> = fx.async {
    repo.run {
        val loginExists = user.doesUserLoginExist().bind()
        if (loginExists) validatorAE.raiseError(UserLoginExits(user.login).nel())
        else validatorAE.just(loginExists)
    }
}

fun <F, S> EffectValidator<F, S, ValidationError>.validateUserWithRules(user: User): Kind<F, Kind<S, Unit>> = fx.async {
    validatorAE.run {
        // 🚩 These are eager calls. So even in fail-fast mode, with `Either` AE
        // all these methods are called even after first `raiseError` on AE
        // as `Either` AE doesn't have that short-circuit functionality. Looking for a better approach.
        mapN(
                validateEmailWithRules(user.email),
                cityShouldBeValid(user).bind(),
                loginShouldNotExit(user).bind()
        ) {}.handleErrorWith { raiseError(it) }
    }
}



