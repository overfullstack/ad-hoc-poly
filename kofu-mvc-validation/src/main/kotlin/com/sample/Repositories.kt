package com.sample


import com.validation.City
import com.validation.User
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class UserRepository(private val client: NamedParameterJdbcTemplate) {

    fun count() =
            client.queryForObject("SELECT COUNT(*) FROM users", emptyMap<String, String>(), Int::class.java)

    fun doesUserExistWith(login: String) =
            client.queryForObject("SELECT 1 FROM users WHERE login = :login LIMIT 1", mapOf("login" to login), Int::class.java) == 1

    fun findAll() = client.queryForList("SELECT * FROM users", emptyMap<String, String>(), User::class.java)

    fun findOne(login: String) =
            client.queryForObject("SELECT * FROM users WHERE login = :login", mapOf("login" to login), User::class.java)

    fun deleteAll() =
            client.execute("DELETE FROM users") {}

    fun update(user: User) =
            client.update("UPDATE users SET  firstname = :firstname, lastname = :lastname WHERE login = :login",
                    BeanPropertySqlParameterSource(user))

    fun save(user: User) =
            client.update("INSERT INTO users (login, firstname, lastname) VALUES (:login, :firstname, :lastname)",
                    BeanPropertySqlParameterSource(user))
}

class CityRepository(private val client: NamedParameterJdbcTemplate) {

    fun count() =
            client.queryForObject("SELECT COUNT(*) FROM city", emptyMap<String, String>(), Int::class.java)

    fun doesCityExitsWith(name: String) =
            client.queryForObject("SELECT 1 FROM city WHERE name = :name LIMIT 1", mapOf("name" to name), Int::class.java) == 1

    fun findAll() = client.queryForList("SELECT * FROM city", emptyMap<String, String>(), City::class.java)

    fun findOne(name: String) =
            client.queryForObject("SELECT * FROM city WHERE name = :name", mapOf("name" to name), City::class.java)

    fun deleteAll() =
            client.execute("DELETE FROM city") {}

    fun save(city: City) =
            client.update("INSERT INTO city (name) VALUES (:name)",
                    BeanPropertySqlParameterSource(city))
}
