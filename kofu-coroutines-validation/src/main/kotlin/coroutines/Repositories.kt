package coroutines

import org.springframework.data.r2dbc.core.*
import top.City
import top.User

class UserRepository(private val client: DatabaseClient) {

    suspend fun count() =
            client.execute("SELECT COUNT(*) FROM users").asType<Long>().fetch().awaitOne()

    suspend fun doesUserExistsWith(login: String) =
            client.execute("SELECT 1 FROM users WHERE login = :login LIMIT 1").bind("login", login).asType<Int>().fetch().awaitOneOrNull() == 1

    fun findAll() =
            client.select().from("users").asType<User>().fetch().flow()

    suspend fun findOne(login: String) =
            client.execute("SELECT * FROM users WHERE login = :login").bind("login", login).asType<User>().fetch().awaitOne()

    suspend fun deleteAll() =
            client.execute("DELETE FROM users").await()

    suspend fun update(user: User) =
            client.update().table<User>().using(user).fetch().awaitRowsUpdated()

    suspend fun insert(user: User) =
            client.insert().into<User>().table("users").using(user).await()
}

class CityRepository(private val client: DatabaseClient) {

    suspend fun count() =
            client.execute("SELECT COUNT(*) FROM city").asType<Long>().fetch().one()

    suspend fun doesCityExistsWith(name: String) =
            client.execute("SELECT 1 FROM city WHERE name = :name LIMIT 1").bind("name", name).asType<Int>().fetch().awaitOneOrNull() == 1

    fun findAll() =
            client.select().from("city").asType<City>().fetch().flow()

    suspend fun findOne(name: String) =
            client.execute("SELECT * FROM city WHERE name = :name").bind("name", name).asType<City>().fetch().awaitOne()

    suspend fun deleteAll() =
            client.execute("DELETE FROM city").await()

    suspend fun insert(city: City) =
            client.insert().into<City>().table("city").using(city).await()
}
