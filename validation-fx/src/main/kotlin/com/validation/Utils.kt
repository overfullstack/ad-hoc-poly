/* gakshintala created on 3/8/20 */
package com.validation

import arrow.fx.extensions.IOAsync
import arrow.fx.reactor.extensions.MonoKAsync
import arrow.fx.reactor.fix
import arrow.fx.reactor.k
import reactor.core.publisher.Mono

fun <R> MonoKAsync.forMono(thunk: suspend () -> Mono<R>) = effect { thunk().k().suspended() }.fix()
fun <R> IOAsync.forIO(thunk: suspend () -> R) = effect { thunk() }

