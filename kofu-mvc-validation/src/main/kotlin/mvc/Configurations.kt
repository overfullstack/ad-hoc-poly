package mvc

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.async.async
import arrow.fx.extensions.io.functor.void
import arrow.fx.handleError
import arrow.fx.typeclasses.Async
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.fu.kofu.configuration
import org.springframework.fu.kofu.webmvc.webMvc
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import top.City
import top.User
import top.ValidationError
import top.typeclass.EffectValidatorFailFast
import top.typeclass.Repo
import top.typeclass.failFast


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
            val userRepository = ref<UserRepository>()
            val cityRepository = ref<CityRepository>()
            object : Repo<ForIO> {
                override fun User.update(): IO<Unit> = IO { userRepository.update(this) }.void()
                override fun User.insert(): IO<Unit> = IO { userRepository.insert(this) }.void()
                override fun User.doesUserLoginExist(): IO<Boolean> = IO { userRepository.doesUserExitsWith(login) }.handleError { false }
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
    userRepository.insert(User("tarkansh", "tarkansh@kt.com", "akshintala", "tark", "london"))
    userRepository.insert(User("mguduri", "mguduri@kt.com", "guduri", "mango", "sydney"))
    userRepository.insert(User("gopals", "gopals@kt.com", "gosh", "shgo", "istanbul"))

    cityRepository.deleteAll()
    cityRepository.insert(City("london"))
    cityRepository.insert(City("sydney"))
    cityRepository.insert(City("istanbul"))
}
