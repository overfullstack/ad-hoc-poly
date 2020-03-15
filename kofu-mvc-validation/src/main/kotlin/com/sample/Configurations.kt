package com.sample

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.async.async
import arrow.fx.handleError
import arrow.fx.typeclasses.Async
import com.validation.City
import com.validation.User
import com.validation.ValidationError
import com.validation.forIO
import com.validation.typeclass.EffectValidator
import com.validation.typeclass.FailFast
import com.validation.typeclass.ForFailFast
import com.validation.typeclass.Repo
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.fu.kofu.configuration
import org.springframework.fu.kofu.webmvc.webMvc
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate


val dataConfig = configuration {
    beans {
        bean {
            val dataSourceBuilder = DataSourceBuilder.create()
            dataSourceBuilder.driverClassName("org.h2.Driver")
            dataSourceBuilder.url("jdbc:h2:mem:test")
            dataSourceBuilder.username("SA")
            dataSourceBuilder.password("")
            dataSourceBuilder.build()
        }
        bean<NamedParameterJdbcTemplate>()
        bean<UserRepository>()
        bean<CityRepository>()
        bean<Repo<ForIO>> {
            object : Repo<ForIO>, Async<ForIO> by IO.async() {
                override fun User.update() = forIO { ref<com.sample.UserRepository>().update(this) }.void()
                override fun User.insert() = forIO { ref<UserRepository>().insert(this) }.void()
            }
        }
        bean<EffectValidator<ForIO, ForFailFast<ValidationError>, ValidationError>> {
            object : EffectValidator<ForIO, ForFailFast<ValidationError>, ValidationError> {
                override val repo = ref<Repo<ForIO>>()
                override val simpleValidator = FailFast<ValidationError>()

                override fun User.doesUserLoginExist() = repo.forIO { ref<UserRepository>().findFirstUserWith(login) }.handleError { false }
                override fun User.isUserCityValid() = repo.forIO { ref<CityRepository>().findFirstCityWith(city) }.handleError { false }
            }
        }
        bean {
            HandlersX(ref())
        }
    }
    listener<ApplicationReadyEvent> {
        init(ref(), ref(), ref())
    }
}

val webConfig = configuration {
    webMvc {
        port = if (profiles.contains("test")) 8181 else 8080
        router {
            val handlers = ref<Handlers>()
            val handlersX = ref<HandlersX>()
            POST("/api/upsert", handlersX::upsertX)
            GET("/api/user/all", handlers::listApi)
        }
        converters {
            string()
            jackson()
        }
    }
}

fun init(
        client: NamedParameterJdbcTemplate,
        userRepository: UserRepository,
        cityRepository: CityRepository
) {
    val createUsers = "CREATE TABLE IF NOT EXISTS users (login varchar PRIMARY KEY, email varchar, firstName varchar, lastName varchar, city varchar);"
    val createCity = "CREATE TABLE IF NOT EXISTS city (name varchar PRIMARY KEY);"
    client.execute(createUsers + createCity)
    { ps -> ps.execute() }

    userRepository.deleteAll()
    userRepository.insert(User("smaldini", "smaldini@kt.com", "Stéphane", "Maldini", "london"))
    userRepository.insert(User("sdeleuze", "sdeleuze@kt.com", "Sébastien", "Deleuze", "sydney"))
    userRepository.insert(User("bclozel", "bclozel@kt.com", "Brian", "Clozel", "istanbul"))

    cityRepository.deleteAll()
    cityRepository.insert(City("london"))
    cityRepository.insert(City("sydney"))
    cityRepository.insert(City("istanbul"))
}
