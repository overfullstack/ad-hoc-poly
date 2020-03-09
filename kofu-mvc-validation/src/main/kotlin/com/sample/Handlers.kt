package com.sample

import arrow.core.fix
import arrow.fx.ForIO
import arrow.fx.fix
import com.validation.*
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.ServerResponse.badRequest
import org.springframework.web.servlet.function.ServerResponse.ok
import org.springframework.web.servlet.function.body

@Suppress("UNUSED_PARAMETER")
class Handlers(private val userRepository: UserRepository,
               private val cityRepository: CityRepository,
               private val blockingRepo: RepoTC<ForIO>) {
    
    fun listApi(request: ServerRequest): ServerResponse {
        return ok().contentType(MediaType.APPLICATION_JSON).body(userRepository.findAll())
    }
    
    fun upsert(request: ServerRequest): ServerResponse {
        val user = request.body<User>()
        val isEmailValid = RulesRunnerStrategy.failFast<ValidationError>().run {
            emailRuleRunner(user.email)
        }.fix()
        return isEmailValid.fold(
                { badRequest().body("$user email validation error: ${it.head}") },
                {
                    if (cityRepository.doesCityExitsWith(user.city)) {
                        if (userRepository.doesUserExistWith(user.login)) {
                            userRepository.update(user)
                            ok().body("Updated!! $user")
                        } else {
                            userRepository.save(user)
                            ok().body("Inserted!! $user")
                        }
                    } else {
                        badRequest().body("com.validation.City is invalid!! : $user")
                    }
                }
        )
    }

    fun upsertX(request: ServerRequest) =
            blockingRepo.run {
                RulesRunnerStrategy.accumulateErrors<ValidationError>().run {
                    validateForUpsert(request.body()).fix().unsafeRunSync()
                }
            }.fold(
                    { badRequest().body(it) },
                    { ok().body(it) }
            )
}
