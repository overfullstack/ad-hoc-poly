package com.validation

import arrow.core.*
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicativeError.applicativeError
import arrow.typeclasses.ApplicativeError

/**
 * A generic rules class that abstracts over validation strategies
 */
interface RuleRunnerStrategy<S, E> : ApplicativeError<S, Nel<E>>

/**
 * Fails fast with `Either`.
 */
class FailFastStrategy<E> : RuleRunnerStrategy<EitherPartialOf<Nel<E>>, E>,
        ApplicativeError<EitherPartialOf<Nel<E>>, Nel<E>> by Either.applicativeError()

/**
 * Accumulates errors with `Validated` and `NonEmptyList`.
 */
class ErrorAccumulationStrategy<E> : RuleRunnerStrategy<ValidatedPartialOf<Nel<E>>, E>,
        ApplicativeError<ValidatedPartialOf<Nel<E>>, Nel<E>> by Validated.applicativeError(NonEmptyList.semigroup())
