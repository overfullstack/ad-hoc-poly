package com.validation.typeclass

import arrow.core.*
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicativeError.applicativeError
import arrow.typeclasses.ApplicativeError

typealias Validator<F, E> = ApplicativeError<F, Nel<E>>

typealias ForFailFast<E> = EitherPartialOf<Nel<E>>
typealias FailFast<E> = Validator<ForFailFast<E>, E>

typealias ForErrorAccumulation<E> = ValidatedPartialOf<Nel<E>>
typealias ErrorAccumulation<E> = Validator<ForErrorAccumulation<E>, E>

fun <E> failFast(): FailFast<E> = object : FailFast<E> by Either.applicativeError() {}
fun <E> errorAccumulation(): ErrorAccumulation<E> = object : ErrorAccumulation<E> by Validated.applicativeError(NonEmptyList.semigroup()) {}
