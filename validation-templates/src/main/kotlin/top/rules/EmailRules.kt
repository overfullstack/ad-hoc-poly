package top.rules

import arrow.Kind
import arrow.core.nel
import top.ValidationError
import top.typeclass.ValidatorAE

/**
 * ------------Email Rules------------
 * Arbitrary rules can be defined anywhere outside the Rules algebra.
 */
fun <S> ValidatorAE<S, ValidationError>.contains(email: String, needle: String): Kind<S, String> =
        if (email.contains(needle, false)) just(email)
        else raiseError(ValidationError.DoesNotContain(needle).nel())

fun <S> ValidatorAE<S, ValidationError>.maxLength(email: String, maxLength: Int): Kind<S, String> =
        if (email.length <= maxLength) just(email)
        else raiseError(ValidationError.EmailMaxLength(maxLength).nel())
