/* gakshintala created on 3/14/20 */
package top.typeclass

import arrow.Kind
import arrow.core.*
import arrow.fx.typeclasses.Async
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import top.User
import top.ValidationError
import top.rules.cityShouldBeValid
import top.rules.loginShouldNotExit

interface EffectValidator<F, S, E> : Async<F> {
    val repo: Repo<F>
    val validatorAE: ValidatorAE<S, E>
}

interface EffectValidatorFailFast<F, E> : EffectValidator<F, ForFailFast<E>, E>
interface EffectValidatorErrorAccumulation<F, E> : EffectValidator<F, ForErrorAccumulation<E>, E>

fun <F> EffectValidatorFailFast<F, ValidationError>.validateUserWithRules(user: User) = fx.async {
        validatorAE.validateEmailWithRules(user.email)
                .flatMap { cityShouldBeValid(user).bind().fix() }
                .flatMap { loginShouldNotExit(user).bind().fix() }
}

fun <F> EffectValidatorErrorAccumulation<F, ValidationError>.validateUserWithRules(user: User): Kind<F, Kind<ValidatedPartialOf<Nel<ValidationError>>, Unit>> = fx.async {
    validatorAE.run {
        mapN( // this run in the context of `validatorAE`
                validateEmailWithRules(user.email),
                cityShouldBeValid(user).bind(),
                loginShouldNotExit(user).bind()
        ) {}
    }
}
