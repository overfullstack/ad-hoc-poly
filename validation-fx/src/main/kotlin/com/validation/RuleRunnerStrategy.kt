package com.validation

import arrow.core.*
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicativeError.applicativeError
import arrow.typeclasses.ApplicativeError

typealias ForFailFast<E> = EitherPartialOf<Nel<E>>
typealias ForErrorAccumulation<E> = ValidatedPartialOf<Nel<E>>

/**
 * A generic rules class that abstracts over validation strategies
 */
interface RuleRunnerStrategy<F, E> : ApplicativeError<F, Nel<E>>

/**
 * Fails fast with `Either`.
 */
class FailFastStrategy<E> : RuleRunnerStrategy<EitherPartialOf<Nel<E>>, E>,
        ApplicativeError<ForFailFast<E>, Nel<E>> by Either.applicativeError()

/**
 * Accumulates errors with `Validated` and `NonEmptyList`.
 */
class ErrorAccumulationStrategy<E> : RuleRunnerStrategy<ValidatedPartialOf<Nel<E>>, E>,
        ApplicativeError<ForErrorAccumulation<E>, Nel<E>> by Validated.applicativeError(NonEmptyList.semigroup())
