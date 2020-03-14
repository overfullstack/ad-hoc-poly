package com.validation.typeclass

import arrow.Kind
import arrow.core.nel
import arrow.fx.typeclasses.Async
import com.validation.User
import com.validation.ValidationError
import com.validation.ValidationError.UserCityInvalid
import com.validation.ValidationError.UserLoginExits
import com.validation.rules.validateEmailWithRules

interface Repo<F, S> {
    val effect: Async<F>
    val ruleRunStrategy: RuleRunStrategy<S, ValidationError>
    
    fun User.doesUserLoginExist(): Kind<F, Boolean>
    fun User.isUserCityValid(): Kind<F, Boolean>
    fun User.update(): Kind<F, Unit>
    fun User.insert(): Kind<F, Unit>
}


