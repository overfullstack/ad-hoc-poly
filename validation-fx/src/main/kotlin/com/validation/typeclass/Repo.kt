package com.validation.typeclass

import arrow.Kind
import arrow.core.Either
import arrow.core.Nel
import arrow.core.left
import arrow.core.right
import arrow.fx.typeclasses.Async
import com.validation.User
import com.validation.ValidationError

interface Repo<F> : Async<F> {
    fun User.update(): Kind<F, Unit>
    fun User.insert(): Kind<F, Unit>

    fun User.toLeft(): (Nel<ValidationError>) -> Either<String, String> = { reasons ->
        when (reasons.head) {
            ValidationError.UserLoginExits(login) -> {
                update()
                "Updated!! $this".right()
            }
            else -> "Cannot Upsert!!, reasons: $reasons".left()
        }
    }

    fun User.toRight(): (Unit) -> String = {
        this.insert()
        "Inserted!! $this"
    }

}


