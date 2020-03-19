package com.validation.rules

import arrow.core.nel
import arrow.fx.typeclasses.MonadDefer
import com.validation.ValidationError
import com.validation.typeclass.ValidatorAE

/**
 * ------------Email Rules------------
 * Arbitrary rules can be defined anywhere outside the Rules algebra.
 */
private fun <S> ValidatorAE<S, ValidationError>.contains(email: String, needle: String) =
        if (email.contains(needle, false)) just(email)
        else raiseError(ValidationError.DoesNotContain(needle).nel())

private fun <S> ValidatorAE<S, ValidationError>.maxLength(email: String, maxLength: Int) =
        if (email.length <= maxLength) just(email)
        else raiseError(ValidationError.MaxLength(maxLength).nel())

/**
 * Some rules that use the applicative syntax to validate and gather errors.
 */
fun <S> ValidatorAE<S, ValidationError>.validateEmailWithRules(email: String) =
    mapN(
            contains(email, "@"),
            maxLength(email, 250)
    ) {}.handleErrorWith { raiseError(it) }
