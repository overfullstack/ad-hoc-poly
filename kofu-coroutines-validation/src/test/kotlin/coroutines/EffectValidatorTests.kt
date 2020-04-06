/* gakshintala created on 3/14/20 */
package coroutines

import arrow.core.fix
import arrow.core.nel
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.async.async
import arrow.fx.fix
import arrow.fx.typeclasses.Async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.WebApplicationType
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.fu.kofu.application
import top.User
import top.ValidationError
import top.ValidationError.*
import top.rules.validateUserWithRules
import top.typeclass.*

class EffectValidatorTests {

    private val dataApp = application(WebApplicationType.NONE) {
        enable(dataConfig)
    }

    private lateinit var context: ConfigurableApplicationContext
    private lateinit var coroutineFFValidator: EffectValidator<ForIO, ForFailFast<ValidationError>, ValidationError>
    private lateinit var coroutineEAValidator: EffectValidator<ForIO, ForErrorAccumulation<ValidationError>, ValidationError>

    @BeforeAll
    fun beforeAll() {
        context = dataApp.run(profiles = "test")
        coroutineFFValidator = context.getBean()
        coroutineEAValidator = object : EffectValidator<ForIO, ForErrorAccumulation<ValidationError>, ValidationError>, Async<ForIO> by IO.async() {
            override val repo = context.getBean<Repo<ForIO>>()
            override val validatorAE = errorAccumulation<ValidationError>()
        }
    }

    @Test
    fun `EA on Valid user`() = runBlocking {
        val validUser = User("gakshintala", "tarkansh@kt.com", "akshintala", "tark", "london")
        val result = coroutineFFValidator.validateUserWithRules(validUser).fix().suspended().fix()
        assertTrue(result.isRight())
    }

    @Test
    fun `EA on only Invalid login`() = runBlocking {
        val invalidUser = User("tarkansh", "tarkansh@kt.com", "akshintala", "tark", "london")
        val result = coroutineFFValidator.validateUserWithRules(invalidUser).fix().suspended().fix()
        result.run {
            assertTrue(isLeft())
            fold({
                assertEquals(1, it.size)
                assertEquals(ValidationError.UserLoginExits("tarkansh"), it.head)
            }, {})
        }
    }

    @Test
    fun `FF on Invalid Email`() = runBlocking {
        val invalidUser = User("gakshintala", "tarkansh-kt.com${(0..251).map { "g" }}", "akshintala", "tark", "london")
        val result = coroutineEAValidator.validateUserWithRules(invalidUser).fix().suspended().fix()
        result.run {
            assertTrue(isInvalid)
            fold({
                assertEquals(2, it.size)
                assertEquals(DoesNotContain("@"), it.head)
            }, {})
        }
    }

    @Test
    fun `EA on Invalid Email No needle + Email Max length + Invalid City`() = runBlocking {
        val invalidUser = User("gakshintala", "tarkansh-kt.com${(0..251).map { "g" }}", "akshintala", "tark", "vja")
        val result = coroutineFFValidator.validateUserWithRules(invalidUser).fix().suspended().fix()
        result.run {
            assertTrue(isLeft())
            fold({
                assertEquals(1, it.size)
                assertEquals(DoesNotContain("@").nel(), it)
            }, {})
        }
    }

    @Test
    fun `FF on Invalid Email No needle + Invalid City`() = runBlocking {
        val invalidUser = User("gakshintala", "tarkansh-kt.com${(0..251).map { "g" }}", "akshintala", "tark", "vja")
        val result = coroutineEAValidator.validateUserWithRules(invalidUser).fix().suspended().fix()
        result.run {
            assertTrue(isInvalid)
            fold({
                assertEquals(3, it.size)
                assertEquals(DoesNotContain("@"), it.head)
            }, {})
        }
    }

    @Test
    fun `FF on Invalid Email Length + Invalid Login`() = runBlocking {
        val invalidUser = User("tarkansh", "tarkansh@kt.com${(0..251).map { "g" }}", "akshintala", "tark", "london")
        val result = coroutineEAValidator.validateUserWithRules(invalidUser).fix().suspended().fix()
        result.run {
            assertTrue(isInvalid)
            fold({
                assertEquals(2, it.size)
                assertEquals(EmailMaxLength(250), it.head)
            }, {})
        }
    }

    @Test
    fun `FF on Invalid City + Invalid login`() = runBlocking {
        val invalidUser = User("tarkansh", "tarkansh@kt.com", "akshintala", "tark", "hyd")
        val result = coroutineEAValidator.validateUserWithRules(invalidUser).fix().suspended().fix()
        result.run {
            assertTrue(isInvalid)
            fold({
                assertEquals(2, it.size)
                assertEquals(UserCityInvalid("hyd"), it.head)
            }, {})
        }
    }

    @Test
    fun `FF on only Invalid login`() = runBlocking {
        val invalidUser = User("tarkansh", "tarkansh@kt.com", "akshintala", "tark", "london")
        val result = coroutineEAValidator.validateUserWithRules(invalidUser).fix().suspended().fix()
        result.run {
            assertTrue(isInvalid)
            fold({
                assertEquals(1, it.size)
                assertEquals(ValidationError.UserLoginExits("tarkansh"), it.head)
            }, {})
        }
    }

    @AfterAll
    fun afterAll() {
        context.close()
    }
}
