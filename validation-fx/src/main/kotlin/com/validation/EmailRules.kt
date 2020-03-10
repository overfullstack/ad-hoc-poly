package com.validation

import arrow.core.nel

/**
 * Arbitrary rules can be defined anywhere outside the Rules algebra.
 */
fun <F> RulesRunnerStrategy<F, ValidationError>.contains(emailValue: String, needle: String) =
        if (emailValue.contains(needle, false)) just(emailValue)
        else raiseError(ValidationError.DoesNotContain(needle).nel())

/**
 * Arbitrary rules can be defined anywhere outside the Rules algebra.
 */
fun <F> RulesRunnerStrategy<F, ValidationError>.maxLength(emailValue: String, maxLength: Int) =
        if (emailValue.length <= maxLength) just(emailValue)
        else raiseError(ValidationError.MaxLength(maxLength).nel())

/**
 * Some rules that use the applicative syntax to validate and gather errors.
 */
fun <F> RulesRunnerStrategy<F, ValidationError>.emailRuleRunner(emailValue: String) =
        mapN(
                contains(emailValue, "@"),
                maxLength(emailValue, 250)
        ) {
                Email(emailValue)
        }.handleErrorWith { raiseError(it) }
