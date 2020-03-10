package com.sample

import arrow.core.fix
import arrow.fx.ForIO
import arrow.fx.fix
import com.validation.*
import com.validation.ValidationError.UserLoginExits
import org.springframework.http.MediaType
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.ServerResponse.badRequest
import org.springframework.web.servlet.function.ServerResponse.ok
import org.springframework.web.servlet.function.body

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
                    if (cityRepository.findFirstCityWith(user.city)) {
                        if (userRepository.findFirstUserWith(user.login)) {
                            userRepository.update(user)
                            ok().body("Updated!! $user")
                        } else {
                            userRepository.insert(user)
                            ok().body("Inserted!! $user")
                        }
                    } else {
                        badRequest().body("City is invalid!! : $user")
                    }
                }
        )
    }

    fun upsertX(request: ServerRequest): ServerResponse {
        val user = request.body<User>()
        return blockingRepo.run {
            RulesRunnerStrategy.failFast<ValidationError>().run {
                userRuleRunner(user).fix().unsafeRunSync()
            }
        }.fix().fold(
                { reasons ->
                    when (reasons.head) {
                        UserLoginExits(user.login) -> {
                            userRepository.update(user)
                            ok().body("Updated!! $user")
                        }
                        else -> badRequest().body("Cannot Upsert!!, reasons: $reasons")
                    }
                },
                {
                    userRepository.insert(user)
                    ok().body("Inserted!! $user")
                }
        )
    }
}
