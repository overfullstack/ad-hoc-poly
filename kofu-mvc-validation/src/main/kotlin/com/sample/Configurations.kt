package com.sample

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.async.async
import arrow.fx.typeclasses.Async
import com.validation.City
import com.validation.RepoTC
import com.validation.User
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
        bean<RepoTC<ForIO>> {
            object : RepoTC<ForIO>, Async<ForIO> by IO.async() {
                override fun User.get() = forIO { ref<UserRepository>().findOne(login) }
                override fun User.doesUserExistWith() = forIO { ref<UserRepository>().doesUserExistWith(login) }
                override fun User.doesCityExistWith() = forIO { ref<UserRepository>().doesUserExistWith(city) }
                override fun User.update() = forIO { ref<UserRepository>().update(this) }
                override fun User.insert() = forIO { ref<UserRepository>().save(this) }
            }
        }
    }
    listener<ApplicationReadyEvent> {
        init(ref(), ref(), ref())
    }
}

fun init(
        client: NamedParameterJdbcTemplate,
        userRepository: UserRepository,
        cityRepository: CityRepository
) {
    val createUsers = "CREATE TABLE IF NOT EXISTS users (login varchar PRIMARY KEY, email varchar, firstname varchar, lastname varchar);"
    val createCity = "CREATE TABLE IF NOT EXISTS city (name varchar PRIMARY KEY);"
    client.execute(createUsers + createCity)
    { ps -> ps.execute() }
    
    userRepository.deleteAll()
    userRepository.save(User("smaldini", "smaldini@kt.com", "Stéphane", "Maldini", "london"))
    userRepository.save(User("sdeleuze", "sdeleuze@kt.com", "Sébastien", "Deleuze", "sydney"))
    userRepository.save(User("bclozel", "bclozel@kt.com", "Brian", "Clozel", "istanbul"))

    cityRepository.deleteAll()
    cityRepository.save(City("london"))
    cityRepository.save(City("sydney"))
    cityRepository.save(City("istanbul"))
}


val webConfig = configuration {
    webMvc {
        port = if (profiles.contains("test")) 8181 else 8080
        router {
            val handler = ref<Handlers>()
            POST("/api/register", handler::registerX)
            POST("/api/upsert", handler::upsertX)
            GET("/api/user/all", handler::listApi)
        }
        converters {
            string()
            jackson()
        }
    }
}
