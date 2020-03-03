package com.sample

import com.validation.Email
import com.validation.RepoTC
import com.validation.Rules
import arrow.core.fix
import arrow.fx.ForIO
import arrow.fx.fix
import com.validation.User
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

    fun userApi(request: ServerRequest) =
            ok().contentType(MediaType.APPLICATION_JSON)
                    .body(userRepository.findOne(request.pathVariable("login")) ?: "USER NOT FOUND")

    fun register(request: ServerRequest): ServerResponse {
        val user = request.body<User>()
        return try {
            userRepository.findOne(user.login)
            badRequest().body("com.validation.User with same login exists!!")
        } catch (ex: EmptyResultDataAccessException) {
            val isEmailValid = Rules failFast {
                Email(user.login).validateEmail().fix()
            }
            isEmailValid.fold(
                    { badRequest().body("$user email validation error: ${it.head}") },
                    {
                        userRepository.save(user)
                        ok().body("Created!! $user")
                    }
            )
        }
    }

    fun registerX(request: ServerRequest) =
            blockingRepo.run {
                request.body<User>().validateUserForRegister().fix().unsafeRunSync()
            }.fold(
                    { badRequest().body(it) },
                    {
                        userRepository.save(it)
                        ok().body("Created!! $it")
                    }
            )

    fun upsert(request: ServerRequest): ServerResponse {
        val user = request.body<User>()
        val isEmailValid = Rules failFast {
            Email(user.login).validateEmail().fix()
        }
        return isEmailValid.fold(
                { badRequest().body("$user email validation error: ${it.head}") },
                {
                    if (cityRepository.doesCityExitsWith(user.city)) {
                        if (userRepository.doesUserExistWith(user.login)) {
                            userRepository.update(user)
                            ok().body("Updated!! $user")
                        } else {
                            userRepository.save(user)
                            ok().body("Created!! $user")
                        }
                    } else {
                        badRequest().body("com.validation.City is invalid!! : $user")
                    }
                }
        )
    }

    fun upsertX(request: ServerRequest) =
            blockingRepo.run {
                request.body<User>().validateUserForUpsert().fix().unsafeRunSync()
            }.fold(
                    { badRequest().body(it) },
                    { ok().body(it) }
            )
}
