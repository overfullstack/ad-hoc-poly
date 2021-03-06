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

fun <F> EffectValidatorFailFast<F, ValidationError>.validateUserWithRules(user: User): Kind<F, Either<NonEmptyList<ValidationError>, Any>> = fx.async {
    // `validateEmailWithRulesResult` is the cheapest validation, which doesn't need any db calls. So this is done first.
    val validateEmailWithRulesResult: Either<NonEmptyList<ValidationError>, String> = validatorAE.validateEmailWithRules(user.email)
    if (validateEmailWithRulesResult.isRight()) {
        // with `bind()`, the `flatMap` is done on F.
        // You can't put `bind()` inside the `flatMap`. So computing them separately and using `flatMap`.
        val cityShouldBeValid: Either<NonEmptyList<ValidationError>, Boolean> = cityShouldBeValid(user).bind().fix()
        val loginShouldNotExit: Either<NonEmptyList<ValidationError>, Boolean> = loginShouldNotExit(user).bind().fix()
        cityShouldBeValid
            .flatMap { loginShouldNotExit }
    } else {
        validateEmailWithRulesResult
    }
}

fun <F> EffectValidatorErrorAccumulation<F, ValidationError>.validateUserWithRules(user: User): Kind<F, Validated<NonEmptyList<ValidationError>, Unit>> = fx.async {
    validatorAE.run {
        mapN( // this runs in the context of `validatorAE`
                validateEmailWithRules(user.email),
                cityShouldBeValid(user).bind(),
                loginShouldNotExit(user).bind()
        ) {}.fix()
    }
}
