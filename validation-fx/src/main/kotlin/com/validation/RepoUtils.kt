/* gakshintala created on 3/8/20 */
package com.validation

import arrow.fx.reactor.k
import arrow.fx.typeclasses.Async
import reactor.core.publisher.Mono

fun <R, F> Async<F>.forMono(thunk: suspend () -> Mono<R>) = effect { thunk().k().suspended() }
fun <R, F> Async<F>.forIO(thunk: suspend () -> R) = effect { thunk() }
