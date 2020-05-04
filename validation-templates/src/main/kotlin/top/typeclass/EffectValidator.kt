/* gakshintala created on 3/14/20 */
package top.typeclass

import arrow.Kind
import arrow.core.*
import arrow.fx.typeclasses.Async
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

fun <F> EffectValidatorFailFast<F, ValidationError>.validateUserWithRules(user: User): Kind<F, Either<NonEmptyList<ValidationError>, Boolean>> = fx.async {
    val validateEmailWithRules: Either<NonEmptyList<ValidationError>, String> = validatorAE.validateEmailWithRules(user.email)
    val cityShouldBeValid: Either<NonEmptyList<ValidationError>, Boolean> = cityShouldBeValid(user).bind().fix()
    val loginShouldNotExit: Either<NonEmptyList<ValidationError>, Boolean> = loginShouldNotExit(user).bind().fix()
    validateEmailWithRules
            .flatMap { cityShouldBeValid(user).bind().fix() }
            .flatMap { loginShouldNotExit(user).bind().fix() }
}

fun <F> EffectValidatorErrorAccumulation<F, ValidationError>.validateUserWithRules(user: User): Kind<F, Validated<NonEmptyList<ValidationError>, Unit>> = fx.async {
    validatorAE.run {
        val validateEmailWithRules: Validated<NonEmptyList<ValidationError>, Unit> = validateEmailWithRules(user.email)
        val cityShouldBeValid: Kind<Kind<ForValidated, NonEmptyList<ValidationError>>, Boolean> = cityShouldBeValid(user).bind()
        val loginShouldNotExit: Kind<Kind<ForValidated, NonEmptyList<ValidationError>>, Boolean> = loginShouldNotExit(user).bind()
        mapN( // this run in the context of `validatorAE`
                validateEmailWithRules(user.email),
                cityShouldBeValid(user).bind(),
                loginShouldNotExit(user).bind()
        ) {}.fix()
    }
}
