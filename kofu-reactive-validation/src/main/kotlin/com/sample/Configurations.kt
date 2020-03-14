/* gakshintala created on 3/3/20 */
package com.sample

import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.extensions.monok.async.async
import arrow.fx.typeclasses.Async
import com.validation.City
import com.validation.RepoTC
import com.validation.User
import com.validation.forMono
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.fu.kofu.configuration
import org.springframework.fu.kofu.r2dbc.r2dbcH2
import org.springframework.fu.kofu.webflux.webFlux

val dataConfig = configuration {
    beans {
        bean<UserRepository>()
        bean<CityRepository>()
        bean<RepoTC<ForMonoK>> {
            object : RepoTC<ForMonoK>, Async<ForMonoK> by MonoK.async() {
                override fun User.doesUserLoginExist() = forMono { ref<UserRepository>().findFirstUserWith(login) }.map { it!! }
                override fun User.isUserCityValid() = forMono { ref<CityRepository>().findFirstCityWith(city) }.map { it!! }
                override fun User.update() = forMono { ref<UserRepository>().update(this) }.map {}
                override fun User.insert() = forMono { ref<UserRepository>().insert(this) }.map {}
            }
        }
    }
    listener<ApplicationReadyEvent> {
        init(ref(), ref(), ref())
    }
    r2dbcH2()
}

val webFlux = configuration {
    webFlux {
        port = if (profiles.contains("test")) 8181 else 8080
        router {
            val handler = ref<UserHandler>()
            POST("/api/upsert", handler::upsertX)
            GET("/api/user/all", handler::listApi)
        }
        codecs {
            string()
            jackson()
        }
    }
}

fun init(client: DatabaseClient,
         userRepository: UserRepository,
         cityRepository: CityRepository
) {
    val createUsers = "CREATE TABLE IF NOT EXISTS users (login varchar PRIMARY KEY, email varchar, firstname varchar, lastname varchar, city varchar);"
    val createCity = "CREATE TABLE IF NOT EXISTS city (name varchar PRIMARY KEY);"
    client.execute(createUsers).then()
            .then(userRepository.deleteAll())
            .then(userRepository.insert(User("smaldini", "smaldini@kt.com", "Stéphane", "Maldini", "london")))
            .then(userRepository.insert(User("sdeleuze", "sdeleuze@kt.com", "Sébastien", "Deleuze", "sydney")))
            .then(userRepository.insert(User("bclozel", "bclozel@kt.com", "Brian", "Clozel", "istanbul")))
            .block()

    client.execute(createCity).then()
            .then(cityRepository.deleteAll())
            .then(cityRepository.save(City("london")))
            .then(cityRepository.save(City("sydney")))
            .then(cityRepository.save(City("istanbul")))
            .block()
}
