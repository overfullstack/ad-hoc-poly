package com.validation

import arrow.Kind
import arrow.core.nel

/**
 * Arbitrary rules can be defined anywhere outside the Rules algebra
 */
fun <F> Rules<F, ValidationError>.contains(emailValue: String, needle: String): Kind<F, String> =
        if (emailValue.contains(needle, false)) just(emailValue)
        else raiseError(ValidationError.DoesNotContain(needle).nel())

/**
 * Arbitrary rules can be defined anywhere outside the Rules algebra
 */
fun <F> Rules<F, ValidationError>.maxLength(emailValue: String, maxLength: Int): Kind<F, String> =
        if (emailValue.length <= maxLength) just(emailValue)
        else raiseError(ValidationError.MaxLength(maxLength).nel())

/**
 * Some rules that use the applicative syntax to validate and gather errors
 */
fun <F> Rules<F, ValidationError>.emailRuleRunner(emailValue: String): Kind<F, Email> =
        mapN(
                contains(emailValue, "@"),
                maxLength(emailValue, 250)
        ) {
            Email(emailValue)
        }.handleErrorWith { raiseError(ValidationError.NotAnEmail(it).nel()) }
