package com.validation

import arrow.Kind
import arrow.core.*
import arrow.core.extensions.fx
import arrow.fx.typeclasses.Async

interface RepoTC<F> : Async<F> {
    fun User.get(): Kind<F, User?>
    fun User.doesUserLoginExist(): Kind<F, Boolean>
    fun User.isUserCityValid(): Kind<F, Boolean>
    fun User.update(): Kind<F, Any?>
    fun User.insert(): Kind<F, Any?>

    /**
     * ------------User Rules------------
     */

    fun <F1> RulesRunnerStrategy<F1, ValidationError>.userCityShouldBeValid(user: User) = fx.async {
        val cityValid = user.isUserCityValid().bind()
        if (cityValid) this@userCityShouldBeValid.just(cityValid)
        else raiseError(ValidationError.UserCityInvalid(user.city).nel())
    }

    fun <F1> RulesRunnerStrategy<F1, ValidationError>.userLoginShouldNotExit(user: User) = fx.async {
        val userExists = user.doesUserLoginExist().bind()
        if (userExists) raiseError(ValidationError.UserLoginExits(user.login).nel())
        else this@userLoginShouldNotExit.just(userExists)
    }

    fun <F1> RulesRunnerStrategy<F1, ValidationError>.userRuleRunner(user: User) = fx.async {
        RulesRunnerStrategy.failFast<ValidationError>().run {
            mapN(
                    emailRuleRunner(user.email),
                    !userCityShouldBeValid(user),
                    !userLoginShouldNotExit(user)
            ) {
                user
            }.handleErrorWith { reasons -> raiseError(ValidationError.InvalidUser(reasons).nel()) }
        }
    }

    fun <F1> RulesRunnerStrategy<F1, ValidationError>.validateForUpsert(user: User) = fx.async {
        userRuleRunner(user).bind().fix().fold(
                { reasons ->
                    when (reasons.head) {
                        ValidationError.UserLoginExits(user.login) -> {
                            user.update()
                            "Updated!! $user".right()
                        }
                        else -> "Cannot Upsert!!, reasons: $reasons".left()
                    }
                },
                {
                    user.insert()
                    "Inserted!! $user".right()
                }
        )
    }

}


