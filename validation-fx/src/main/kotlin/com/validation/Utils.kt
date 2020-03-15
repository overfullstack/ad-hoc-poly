/* gakshintala created on 3/8/20 */
package com.validation

import arrow.fx.ForIO
import arrow.fx.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import arrow.fx.reactor.k
import arrow.fx.typeclasses.Async
import com.validation.typeclass.Repo
import reactor.core.publisher.Mono

fun <F, R> F.forMono(thunk: suspend () -> Mono<R>) where F : Async<ForMonoK>, F : Repo<ForMonoK> = effect { thunk().k().suspended() }.fix()
fun <F, R> F.forIO(thunk: suspend () -> R) where F : Async<ForIO>, F : Repo<ForIO> = effect { thunk() }.fix()
