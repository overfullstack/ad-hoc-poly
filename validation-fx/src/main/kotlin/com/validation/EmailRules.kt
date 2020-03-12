package com.validation

import arrow.core.nel

/**
 * Arbitrary rules can be defined anywhere outside the Rules algebra.
 */
fun <F> RulesRunnerStrategy<F, ValidationError>.contains(email: String, needle: String) =
        if (email.contains(needle, false)) just(email)
        else raiseError(ValidationError.DoesNotContain(needle).nel())

/**
 * Arbitrary rules can be defined anywhere outside the Rules algebra.
 */
fun <F> RulesRunnerStrategy<F, ValidationError>.maxLength(email: String, maxLength: Int) =
        if (email.length <= maxLength) just(email)
        else raiseError(ValidationError.MaxLength(maxLength).nel())

/**
 * Some rules that use the applicative syntax to validate and gather errors.
 */
fun <F> RulesRunnerStrategy<F, ValidationError>.emailRuleRunner(email: String) =
        mapN(
                contains(email, "@"),
                maxLength(email, 250)
        ) {
                email
        }.handleErrorWith { raiseError(it) }
