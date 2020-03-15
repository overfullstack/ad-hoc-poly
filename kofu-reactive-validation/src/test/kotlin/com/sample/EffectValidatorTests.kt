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
import com.validation.typeclass.EffectValidator
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.WebApplicationType
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.fu.kofu.application

class EffectValidatorTests {

    private val dataApp = application(WebApplicationType.NONE) {
        enable(dataConfig)
    }

    private lateinit var context: ConfigurableApplicationContext
    private lateinit var effectFailFastValidator: EffectValidator<ForMonoK, ValidatedPartialOf<Nel<ValidationError>>, ValidationError>

    @BeforeAll
    fun beforeAll() {
        context = dataApp.run(profiles = "test")
        effectFailFastValidator = context.getBean()
    }

    @Test
    fun `Rule Runner on Valid user`() {
        val validUser = User("gakshintala", "smaldini@kt.com", "Stéphane", "Maldini", "london")
        val result = effectFailFastValidator.validateWithRules(validUser).fix().mono.block()?.fix()
        assertTrue(result?.isValid ?: false)
    }

    @AfterAll
    fun afterAll() {
        context.close()
    }
}
