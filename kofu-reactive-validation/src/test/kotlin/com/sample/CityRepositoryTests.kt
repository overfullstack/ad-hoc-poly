package com.sample

import arrow.core.Nel
import arrow.core.ValidatedPartialOf
import arrow.core.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import com.validation.RepoTC
import com.validation.User
import com.validation.ValidationError
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
    private lateinit var repo: RepoTC<ForMonoK, ValidatedPartialOf<Nel<ValidationError>>>

    @BeforeAll
    fun beforeAll() {
        context = dataApp.run(profiles = "test")
        repository = context.getBean()
        repo = context.getBean()
    }

    @Test
    fun cityNameValid() {
        assertTrue(repository.findFirstCityWith("istanbul").block()!!)
    }

    @Test
    fun cityNameInValid() {
        assertFalse(repository.findFirstCityWith("hyderabad").block()!!)
    }

    @Test
    fun `Rule Runner on Valid user`() {
        val validUser = User("gakshintala", "smaldini@kt.com", "Stéphane", "Maldini", "london")
        val result = repo.run {
            validUser.userRuleRunner()
        }.fix().mono.block()?.fix()
        assertTrue(result?.isValid ?: false)
    }

    @AfterAll
    fun afterAll() {
        context.close()
    }
}
