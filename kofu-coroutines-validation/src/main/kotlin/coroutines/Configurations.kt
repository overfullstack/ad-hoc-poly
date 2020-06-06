/* gakshintala created on 3/3/20 */
package coroutines

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.async.async
import arrow.fx.extensions.io.functor.void
import arrow.fx.handleError
import arrow.fx.typeclasses.Async
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.await
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
        bean<Repo<ForIO>> {
            val userRepository = ref<UserRepository>()
            val cityRepository = ref<CityRepository>()
            object : Repo<ForIO> {
                override fun User.update(): IO<Unit> = IO { userRepository.update(this) }.void()
                override fun User.insert(): IO<Unit> = IO { userRepository.insert(this) }.void()
                override fun User.doesUserLoginExist(): IO<Boolean> = IO { userRepository.doesUserExistsWith(login) }.handleError { false }
                override fun User.isUserCityValid(): IO<Boolean> = IO { cityRepository.doesCityExistsWith(city) }.handleError { false }
            }
        }
        bean<EffectValidatorFailFast<ForIO, ValidationError>> {
            object : EffectValidatorFailFast<ForIO, ValidationError>, Async<ForIO> by IO.async() {
                override val repo = ref<Repo<ForIO>>()
                override val validatorAE = failFast<ValidationError>()
            }
        }
        bean {
            HandlersX(ref())
        }
    }
    listener<ApplicationReadyEvent> {
        runBlocking {
            init(ref(), ref(), ref())
        }
    }
    r2dbcH2()
}

fun Any?.toUnit() = Unit

val webFlux = configuration {
    webFlux {
        port = if (profiles.contains("test")) 8181 else 8080
        coRouter {
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

suspend fun init(
        client: DatabaseClient,
        userRepository: UserRepository,
        cityRepository: CityRepository
) {
    val createUsers = "CREATE TABLE IF NOT EXISTS users (login varchar PRIMARY KEY, email varchar, first_name varchar, last_name varchar, city varchar);"
    val createCity = "CREATE TABLE IF NOT EXISTS city (name varchar PRIMARY KEY);"
    client.execute(createUsers).await()
    userRepository.deleteAll()
    userRepository.insert(User("tarkansh", "tarkansh@kt.com", "akshintala", "tark", "london"))
    userRepository.insert(User("mguduri", "mguduri@kt.com", "guduri", "mango", "sydney"))
    userRepository.insert(User("gopals", "gopals@kt.com", "gosh", "shgo", "istanbul"))
    client.execute(createCity).await()
    cityRepository.deleteAll()
    cityRepository.insert(City("london"))
    cityRepository.insert(City("sydney"))
    cityRepository.insert(City("istanbul"))
}
