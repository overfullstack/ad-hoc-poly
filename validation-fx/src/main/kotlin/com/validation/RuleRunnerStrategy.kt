package com.validation

import arrow.core.*
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicativeError.applicativeError
import arrow.typeclasses.ApplicativeError

/**
 * A generic rules class that abstracts over validation strategies
 */
sealed class RuleRunnerStrategy<F, E>(A: ApplicativeError<F, Nel<E>>) : ApplicativeError<F, Nel<E>> by A {

    /**
     * Accumulates errors thanks to validated and non empty list
     */
    class ErrorAccumulationStrategy<E> :
            RuleRunnerStrategy<ValidatedPartialOf<Nel<E>>, E>(Validated.applicativeError(NonEmptyList.semigroup()))

    /**
     * Fails fast thanks to Either
     */
    class FailFastStrategy<E> :
            RuleRunnerStrategy<EitherPartialOf<Nel<E>>, E>(Either.applicativeError())

    /**
     * DSL
     */
    companion object {
        fun <E> failFast(): FailFastStrategy<E> = FailFastStrategy()
        fun <E> accumulateErrors(): ErrorAccumulationStrategy<E> = ErrorAccumulationStrategy()
    }
}
