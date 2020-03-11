package com.sample

import com.validation.City
import com.validation.User
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.asType
import org.springframework.data.r2dbc.core.into
import org.springframework.data.r2dbc.core.table

class UserRepository(private val client: DatabaseClient) {

    fun count() =
            client.execute("SELECT COUNT(*) FROM users").asType<Long>().fetch().one()

    fun findFirstUserWith(login: String) =
            client.execute("SELECT 1 FROM users WHERE login = :login LIMIT 1").bind("login", login).asType<Int>().fetch().one()
                    .map { it == 1 }
                    .defaultIfEmpty(false)

    fun findAll() =
            client.select().from("users").asType<User>().fetch().all()

    fun findOne(login: String) =
            client.execute("SELECT * FROM users WHERE login = :login").bind("login", login).asType<User>().fetch().one()

    fun deleteAll() =
            client.execute("DELETE FROM users").fetch().one().then()

    fun update(user: User) =
            client.update().table<User>().using(user).then()

    fun insert(user: User) =
            client.insert().into<User>().table("users").using(user).then()
}

class CityRepository(private val client: DatabaseClient) {

    fun count() =
            client.execute("SELECT COUNT(*) FROM city").asType<Long>().fetch().one()

    fun findFirstCityWith(name: String) =
            client.execute("SELECT 1 FROM city WHERE name = :name LIMIT 1").bind("name", name).asType<Int>().fetch().one()
                    .map { it == 1 }
                    .defaultIfEmpty(false)

    fun findAll() =
            client.select().from("city").asType<City>().fetch().all()

    fun findOne(name: String) =
            client.execute("SELECT * FROM city WHERE name = :name").bind("name", name).asType<City>().fetch().one()

    fun deleteAll() =
            client.execute("DELETE FROM city").fetch().one().then()

    fun save(city: City) =
            client.insert().into<City>().table("city").using(city).then()
}



