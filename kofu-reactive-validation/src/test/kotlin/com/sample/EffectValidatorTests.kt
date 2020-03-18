/* gakshintala created on 3/14/20 */
package com.sample

import arrow.core.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.fix
import com.validation.User
import com.validation.ValidationError
import com.validation.rules.validateUserWithRules
import com.validation.typeclass.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
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
    private lateinit var nonBlockingEAValidator: EffectValidator<ForMonoK, ForErrorAccumulation<ValidationError>, ValidationError>
    private lateinit var nonBlockingFFValidator: EffectValidator<ForMonoK, ForFailFast<ValidationError>, ValidationError>

    @BeforeAll
    fun beforeAll() {
        context = dataApp.run(profiles = "test")
        nonBlockingEAValidator = context.getBean()
        nonBlockingFFValidator = object : EffectValidator<ForMonoK, ForFailFast<ValidationError>, ValidationError> {
            override val repo = context.getBean<Repo<ForMonoK>>()
            override val validatorAE = failFast<ValidationError>()
        }
    }

    @Test
    fun `EA on Valid user`() {
        val validUser = User("gakshintala", "smaldini@kt.com", "Stéphane", "Maldini", "london")
        val result = nonBlockingEAValidator.validateUserWithRules(validUser).fix().mono.block()?.fix()
        assertTrue(result?.isValid ?: false)
    }

    @Test
    fun `FF on Invalid Email`() {
        val invalidUser = User("gakshintala", "smaldini-kt.com${(0..251).map { "g" }}", "Stéphane", "Maldini", "london")
        val result = nonBlockingFFValidator.validateUserWithRules(invalidUser).fix().mono.block()?.fix()
        result?.run {
            assertTrue(isLeft())
            fold({
                assertEquals(1, it.size)
                assertEquals(ValidationError.DoesNotContain("@"), it.head)
            }, {})
        }
    }

    @Test
    fun `FF on Invalid Email + Invalid City`() {
        val invalidUser = User("gakshintala", "smaldini-kt.com${(0..251).map { "g" }}", "Stéphane", "Maldini", "vja")
        val result = nonBlockingFFValidator.validateUserWithRules(invalidUser).fix().mono.block()?.fix()
        result?.run {
            assertTrue(isLeft())
            fold({
                assertEquals(1, it.size)
                assertEquals(ValidationError.DoesNotContain("@"), it.head)
            }, {})
        }
    }

    @Test
    fun `FF on Invalid Email + Invalid Login`() {
        val invalidUser = User("smaldini", "smaldini-kt.com${(0..251).map { "g" }}", "Stéphane", "Maldini", "london")
        val result = nonBlockingFFValidator.validateUserWithRules(invalidUser).fix().mono.block()?.fix()
        result?.run {
            assertTrue(isLeft())
            fold({
                assertEquals(1, it.size)
                assertEquals(ValidationError.DoesNotContain("@"), it.head)
            }, {})
        }
    }

    @AfterAll
    fun afterAll() {
        context.close()
    }
}
