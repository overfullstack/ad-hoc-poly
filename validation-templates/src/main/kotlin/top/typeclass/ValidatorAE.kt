package top.typeclass

import arrow.core.*
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicativeError.applicativeError
import arrow.typeclasses.ApplicativeError

typealias ValidatorAE<S, E> = ApplicativeError<S, Nel<E>> // AE needs an effect `S` to manage `E` 

typealias ForFailFast<E> = EitherPartialOf<Nel<E>> // Effect
typealias FailFast<E> = ValidatorAE<ForFailFast<E>, E>

typealias ForErrorAccumulation<E> = ValidatedPartialOf<Nel<E>> // Effect
typealias ErrorAccumulation<E> = ValidatorAE<ForErrorAccumulation<E>, E>

fun <E> failFast(): FailFast<E> = Either.applicativeError()
fun <E> errorAccumulation(): ErrorAccumulation<E> = Validated.applicativeError(NonEmptyList.semigroup())
