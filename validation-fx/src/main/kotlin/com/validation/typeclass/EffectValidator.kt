/* gakshintala created on 3/14/20 */
package com.validation.typeclass

interface EffectValidator<F, S, E> {
    val repo: Repo<F>
    val validatorAE: ValidatorAE<S, E>
}
