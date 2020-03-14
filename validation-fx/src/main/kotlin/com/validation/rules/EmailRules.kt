package com.validation.rules

import arrow.core.nel
import com.validation.typeclass.RuleRunStrategy
import com.validation.ValidationError

/**
 * ------------Email Rules------------
 * Arbitrary rules can be defined anywhere outside the Rules algebra.
 */
private fun <F> RuleRunStrategy<F, ValidationError>.contains(email: String, needle: String) =
        if (email.contains(needle, false)) just(email)
        else raiseError(ValidationError.DoesNotContain(needle).nel())

/**
 * Arbitrary rules can be defined anywhere outside the Rules algebra.
 */
private fun <F> RuleRunStrategy<F, ValidationError>.maxLength(email: String, maxLength: Int) =
        if (email.length <= maxLength) just(email)
        else raiseError(ValidationError.MaxLength(maxLength).nel())

/**
 * Some rules that use the applicative syntax to validate and gather errors.
 */
fun <F> RuleRunStrategy<F, ValidationError>.validateEmailWithRules(email: String) =
        mapN(
                contains(email, "@"),
                maxLength(email, 250)
        ) {
            email
        }.handleErrorWith { raiseError(it) }
