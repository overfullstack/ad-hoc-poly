/* gakshintala created on 3/14/20 */
package com.validation.typeclass

import arrow.Kind
import com.validation.User

interface EffectValidator<F, S, E> {
    val repo: Repo<F>
    val validator: Validator<S, E>

    fun User.doesUserLoginExist(): Kind<F, Boolean>
    fun User.isUserCityValid(): Kind<F, Boolean>
}
