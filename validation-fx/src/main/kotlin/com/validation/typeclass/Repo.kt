package com.validation.typeclass

import arrow.Kind
import arrow.Kind2
import arrow.core.Either
import arrow.core.Nel
import arrow.core.left
import arrow.core.right
import arrow.fx.typeclasses.Async
import arrow.typeclasses.Bifunctor
import com.validation.User
import com.validation.ValidationError

interface Repo<F> : Async<F> {
    fun User.update(): Kind<F, Unit>
    fun User.insert(): Kind<F, Unit>

    fun User.doesUserLoginExist(): Kind<F, Boolean>
    fun User.isUserCityValid(): Kind<F, Boolean>

    fun User.toLeft(): (Nel<ValidationError>) -> Either<String, String> = { reasons ->
        when (reasons.head) {
            ValidationError.UserLoginExits(login) -> {
                update()
                "Updated!! $this".right()
            }
            else -> "Cannot Upsert!!, reasons: $reasons".left()
        }
    }

    fun User.toRight(): (Any) -> String = {
        this.insert()
        "Inserted!! $this"
    }

    fun <S> User.upsert(BF: Bifunctor<S>, result: Kind2<S, Nel<ValidationError>, Any>) =
            BF.run { result.bimap(toLeft(), toRight()) }
}


