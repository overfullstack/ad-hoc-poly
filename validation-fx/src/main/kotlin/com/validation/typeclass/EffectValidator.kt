/* gakshintala created on 3/14/20 */
package com.validation.typeclass

import arrow.fx.typeclasses.Async

interface EffectValidator<F, S, E>: Async<F> {
    val repo: Repo<F>
    val validatorAE: ValidatorAE<S, E>
}
