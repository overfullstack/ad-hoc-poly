/* gakshintala created on 3/8/20 */
package com.validation

import arrow.fx.ForIO
import arrow.fx.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import arrow.fx.reactor.k
import com.validation.typeclass.Repo
import reactor.core.publisher.Mono

fun <R> Repo<ForMonoK>.forMono(thunk: suspend () -> Mono<R>) = effect { thunk().k().suspended() }.fix()
fun <R> Repo<ForIO>.forIO(thunk: suspend () -> R) = effect { thunk() }.fix()
