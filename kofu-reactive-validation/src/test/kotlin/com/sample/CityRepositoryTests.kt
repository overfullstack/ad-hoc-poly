package com.sample

import arrow.core.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import com.validation.RepoTC
import com.validation.RulesRunnerStrategy
import com.validation.User
import com.validation.ValidationError
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.WebApplicationType
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.fu.kofu.application

class CityRepositoryTests {

    private val dataApp = application(WebApplicationType.NONE) {
        enable(dataConfig)
    }

    private lateinit var context: ConfigurableApplicationContext
    private lateinit var repository: CityRepository
    private lateinit var repo: RepoTC<ForMonoK>

    @BeforeAll
    fun beforeAll() {
        context = dataApp.run(profiles = "test")
        repository = context.getBean()
        repo = context.getBean()
    }

    @Test
    fun cityNameValid() {
        assertTrue(repository.findFirstCityWith("istanbul").block() == 1)
    }

    @Test
    fun cityNameInValid() {
        assertTrue(repository.findFirstCityWith("hyderabad").block() == null)
        assertFalse(repository.findFirstCityWith("hyderabad").block() == 1)
    }

    @Test
    fun userCityShouldBeValidForValidCity() {
        val validUser = User("smaldini", "smaldini@kt.com", "Stéphane", "Maldini", "london")
        val result = repo.run {
            RulesRunnerStrategy.accumulateErrors<ValidationError>().run {
                userCityShouldBeValid(validUser)
            }
        }.fix().mono.block()?.fix()
        assertTrue(result?.isValid ?: false)
    }

    @Test
    fun userCityShouldBeValidForInValidCity() {
        val validUser = User("smaldini", "smaldini@kt.com", "Stéphane", "Maldini", "hyderabad")
        val result = repo.run {
            RulesRunnerStrategy.accumulateErrors<ValidationError>().run {
                userCityShouldBeValid(validUser)
            }
        }.fix().mono.block()?.fix()
        assertTrue(result?.isInvalid ?: true)
    }

    @Test
    fun userRuleRunnerTest() {
        val validUser = User("gakshintala", "smaldini@kt.com", "Stéphane", "Maldini", "london")
        val result = repo.run {
            RulesRunnerStrategy.accumulateErrors<ValidationError>().run {
                userRuleRunner(validUser)
            }
        }.fix().mono.block()?.fix()
        assertTrue(result?.isValid ?: false)
    }

    @AfterAll
    fun afterAll() {
        context.close()
    }
}
