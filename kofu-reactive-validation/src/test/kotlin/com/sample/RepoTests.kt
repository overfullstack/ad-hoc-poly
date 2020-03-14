/* gakshintala created on 3/14/20 */
package com.sample

import arrow.core.Nel
import arrow.core.ValidatedPartialOf
import arrow.core.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import com.validation.User
import com.validation.ValidationError
import com.validation.rules.validateWithRules
import com.validation.typeclass.Repo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.WebApplicationType
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.fu.kofu.application

class RepoTests {

    private val dataApp = application(WebApplicationType.NONE) {
        enable(dataConfig)
    }

    private lateinit var context: ConfigurableApplicationContext
    private lateinit var repo: Repo<ForMonoK, ValidatedPartialOf<Nel<ValidationError>>>

    @BeforeAll
    fun beforeAll() {
        context = dataApp.run(profiles = "test")
        repo = context.getBean()
    }

    @Test
    fun `Rule Runner on Valid user`() {
        val validUser = User("gakshintala", "smaldini@kt.com", "Stéphane", "Maldini", "london")
        val result = repo.validateWithRules(validUser).fix().mono.block()?.fix()
        assertTrue(result?.isValid ?: false)
    }

    @AfterAll
    fun afterAll() {
        context.close()
    }
}
