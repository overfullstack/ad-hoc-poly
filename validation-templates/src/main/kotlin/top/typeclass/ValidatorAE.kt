package top.typeclass

import arrow.core.*
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicativeError.applicativeError
import arrow.typeclasses.ApplicativeError
import top.ValidationError
import top.rules.contains
import top.rules.maxLength

typealias ValidatorAE<S, E> = ApplicativeError<S, Nel<E>> // AE needs an Effect `S` to manage Error `E` (for FailFast or ErrorAccumulation)

typealias ForFailFast<E> = EitherPartialOf<Nel<E>> // Effect S
typealias FailFast<E> = ValidatorAE<ForFailFast<E>, E>

typealias ForErrorAccumulation<E> = ValidatedPartialOf<Nel<E>> // Effect S
typealias ErrorAccumulation<E> = ValidatorAE<ForErrorAccumulation<E>, E>

fun <E> failFast(): FailFast<E> = Either.applicativeError()
fun <E> errorAccumulation(): ErrorAccumulation<E> = Validated.applicativeError(NonEmptyList.semigroup())

fun ErrorAccumulation<ValidationError>.validateEmailWithRules(email: String): Validated<NonEmptyList<ValidationError>, Unit> =
        mapN(
                contains(email, "@"),
                maxLength(email, 250)
        ) {}.fix()

fun FailFast<ValidationError>.validateEmailWithRules(email: String): Either<NonEmptyList<ValidationError>, String> =
        contains(email, "@")
                .flatMap { maxLength(email, 250).fix() }
