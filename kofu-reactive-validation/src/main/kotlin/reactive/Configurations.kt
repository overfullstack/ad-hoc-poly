/* gakshintala created on 3/3/20 */
package reactive

import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.extensions.monok.async.async
import arrow.fx.reactor.extensions.monok.functor.void
import arrow.fx.reactor.k
import arrow.fx.typeclasses.Async
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.fu.kofu.configuration
import org.springframework.fu.kofu.r2dbc.r2dbcH2
import org.springframework.fu.kofu.webflux.webFlux
import top.City
import top.User
import top.ValidationError
import top.typeclass.*

val dataConfig = configuration {
    beans {
        bean<UserRepository>()
        bean<CityRepository>()
        bean<Repo<ForMonoK>> {
            val userRepository = ref<UserRepository>()
            val cityRepository = ref<CityRepository>()
            object : Repo<ForMonoK> {
                override fun User.update(): MonoK<Unit> = userRepository.update(this).k().void()
                override fun User.insert(): MonoK<Unit> = userRepository.insert(this).k().void()
                override fun User.doesUserLoginExist(): MonoK<Boolean> = userRepository.doesUserExistsWith(login).k().map { it!! }
                override fun User.isUserCityValid(): MonoK<Boolean> = cityRepository.doesCityExistsWith(city).k().map { it!! }
            }
        }
        bean<EffectValidatorErrorAccumulation<ForMonoK, ValidationError>> {
            object : EffectValidatorErrorAccumulation<ForMonoK, ValidationError>, Async<ForMonoK> by MonoK.async() {
                override val repo = ref<Repo<ForMonoK>>()
                override val validatorAE = errorAccumulation<ValidationError>()
            }
        }
        bean {
            HandlersX(ref())
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
            val handlers = ref<Handlers>()
            val handlersX = ref<HandlersX>()
            POST("/api/upsert", handlersX::upsertX)
            GET("/api/user/all", handlers::listApi)
        }
        codecs {
            string()
            jackson()
        }
    }
}

fun init(
        client: DatabaseClient,
        userRepository: UserRepository,
        cityRepository: CityRepository
) {
    val createUsers = "CREATE TABLE IF NOT EXISTS users (login varchar PRIMARY KEY, email varchar, first_name varchar, last_name varchar, city varchar);"
    val createCity = "CREATE TABLE IF NOT EXISTS city (name varchar PRIMARY KEY);"
    client.execute(createUsers).then()
            .then(userRepository.deleteAll())
            .then(userRepository.insert(User("tarkansh", "tarkansh@kt.com", "akshintala", "tark", "london")))
            .then(userRepository.insert(User("mguduri", "mguduri@kt.com", "guduri", "mango", "sydney")))
            .then(userRepository.insert(User("gopals", "gopals@kt.com", "gosh", "shgo", "istanbul")))
            .block()

    client.execute(createCity).then()
            .then(cityRepository.deleteAll())
            .then(cityRepository.insert(City("london")))
            .then(cityRepository.insert(City("sydney")))
            .then(cityRepository.insert(City("istanbul")))
            .block()
}
