package com.validation.rules

import arrow.core.nel
import com.validation.ValidationError
import com.validation.typeclass.SimpleValidator

/**
 * ------------Email Rules------------
 * Arbitrary rules can be defined anywhere outside the Rules algebra.
 */
private fun <F> SimpleValidator<F, ValidationError>.contains(email: String, needle: String) =
        if (email.contains(needle, false)) just(email)
        else raiseError(ValidationError.DoesNotContain(needle).nel())

/**
 * Arbitrary rules can be defined anywhere outside the Rules algebra.
 */
private fun <F> SimpleValidator<F, ValidationError>.maxLength(email: String, maxLength: Int) =
        if (email.length <= maxLength) just(email)
        else raiseError(ValidationError.MaxLength(maxLength).nel())

/**
 * Some rules that use the applicative syntax to validate and gather errors.
 */
fun <F> SimpleValidator<F, ValidationError>.validateEmailWithRules(email: String) =
        mapN(
                contains(email, "@"),
                maxLength(email, 250)
        ) {
            email
        }.handleErrorWith { raiseError(it) }
