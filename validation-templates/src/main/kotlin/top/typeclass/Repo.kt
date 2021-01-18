package top.typeclass

import arrow.Kind
import arrow.Kind2
import arrow.core.Either
import arrow.core.Nel
import arrow.core.left
import arrow.core.right
import arrow.typeclasses.Bifunctor
import top.User
import top.ValidationError

interface Repo<F> {
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

    // In this case, `Bifunctor` let's us abstract two kinds of Bifunctors we are dealing with `Either` and `Validated`.
    fun <S> User.upsert(BF: Bifunctor<S>, result: Kind2<S, Nel<ValidationError>, Any>): Kind<Kind<S, Either<String, String>>, String> =
            BF.run { result.bimap(toLeft(), toRight()) }
}
