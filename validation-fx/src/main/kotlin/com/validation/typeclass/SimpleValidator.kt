package com.validation.typeclass

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
interface SimpleValidator<F, E> : ApplicativeError<F, Nel<E>>

/**
 * Fails fast with `Either`.
 */
class FailFast<E> : SimpleValidator<ForFailFast<E>, E>,
        ApplicativeError<ForFailFast<E>, Nel<E>> by Either.applicativeError()

/**
 * Accumulates errors with `Validated` and `NonEmptyList`.
 */
class ErrorAccumulation<E> : SimpleValidator<ForErrorAccumulation<E>, E>,
        ApplicativeError<ForErrorAccumulation<E>, Nel<E>> by Validated.applicativeError(NonEmptyList.semigroup())
