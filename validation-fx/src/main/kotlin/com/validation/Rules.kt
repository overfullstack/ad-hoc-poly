package com.validation

import arrow.Kind
import arrow.core.*
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicativeError.applicativeError
import arrow.typeclasses.ApplicativeError

sealed class ValidationError(val msg: String) {
    data class DoesNotContain(val value: String) : ValidationError("Did not contain $value")
    data class MaxLength(val value: Int) : ValidationError("Exceeded length of $value")
    data class NotAnEmail(val reasons: Nel<ValidationError>) : ValidationError("Not a valid email")
}

data class Email(val value: String)

sealed class Rules<F>(A: ApplicativeError<F, Nel<ValidationError>>) : ApplicativeError<F, Nel<ValidationError>> by A {

    private fun Email.contains(needle: String): Kind<F, Email> =
            if (value.contains(needle, false)) just(this)
            else raiseError(ValidationError.DoesNotContain(needle).nel())

    private fun Email.maxLength(maxLength: Int): Kind<F, Email> =
            if (value.length <= maxLength) just(this)
            else raiseError(ValidationError.MaxLength(maxLength).nel())

    fun Email.validateEmail(): Kind<F, String> =
            mapN(
                    contains("@"),
                    maxLength(250)
            ) {
                value
            }.handleErrorWith { raiseError(ValidationError.NotAnEmail(it).nel()) }

    
    object ErrorAccumulationStrategy :
            Rules<ValidatedPartialOf<Nel<ValidationError>>>(Validated.applicativeError(NonEmptyList.semigroup()))

    object FailFastStrategy :
            Rules<EitherPartialOf<Nel<ValidationError>>>(Either.applicativeError())

    companion object {
        infix fun <A> failFast(f: FailFastStrategy.() -> A): A = f(FailFastStrategy)
        infix fun <A> accumulateErrors(f: ErrorAccumulationStrategy.() -> A): A = f(ErrorAccumulationStrategy)
    }
}

