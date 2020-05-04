/* gakshintala created on 3/14/20 */
package reactive

import arrow.core.NonEmptyList
import arrow.core.fix
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.extensions.monok.async.async
import arrow.fx.reactor.fix
import arrow.fx.typeclasses.Async
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
import top.typeclass.*

class EffectValidatorTests {

    private val dataApp = application(WebApplicationType.NONE) {
        enable(dataConfig)
    }

    private lateinit var context: ConfigurableApplicationContext
    private lateinit var nonBlockingEAValidator: EffectValidatorErrorAccumulation<ForMonoK, ValidationError>
    private lateinit var nonBlockingFFValidator: EffectValidatorFailFast<ForMonoK, ValidationError>

    @BeforeAll
    fun beforeAll() {
        context = dataApp.run(profiles = "test")
        nonBlockingEAValidator = context.getBean()
        nonBlockingFFValidator = object : EffectValidatorFailFast<ForMonoK, ValidationError>, Async<ForMonoK> by MonoK.async() {
            override val repo = context.getBean<Repo<ForMonoK>>()
            override val validatorAE = failFast<ValidationError>()
        }
    }

    @Test
    fun `EA on Valid user`() {
        val validUser = User("gakshintala", "tarkansh@kt.com", "akshintala", "tark", "london")
        val result = nonBlockingEAValidator.validateUserWithRules(validUser).fix().mono.block()?.fix()
        assertTrue(result?.isValid ?: false)
    }

    @Test
    fun `EA on only Invalid login`() {
        val invalidUser = User("tarkansh", "tarkansh@kt.com", "akshintala", "tark", "london")
        val result = nonBlockingEAValidator.validateUserWithRules(invalidUser).fix().mono.block()?.fix()
        result?.run {
            assertTrue(isInvalid)
            fold({
                assertEquals(1, it.size)
                assertEquals(ValidationError.UserLoginExits("tarkansh"), it.head)
            }, {})
        }
    }

    @Test
    fun `FF on Invalid Email`() {
        val invalidUser = User("gakshintala", "tarkansh-kt.com${(0..251).map { "g" }}", "akshintala", "tark", "london")
        val result = nonBlockingFFValidator.validateUserWithRules(invalidUser).fix().mono.block()?.fix()
        result?.run {
            assertTrue(isLeft())
            fold({
                assertEquals(1, it.size)
                assertEquals(DoesNotContain("@"), it.head)
            }, {})
        }
    }

    @Test
    fun `EA on Invalid Email No needle + Email Max length + Invalid City`() {
        val invalidUser = User("gakshintala", "tarkansh-kt.com${(0..251).map { "g" }}", "akshintala", "tark", "vja")
        val result = nonBlockingEAValidator.validateUserWithRules(invalidUser).fix().mono.block()?.fix()
        result?.run {
            assertTrue(isInvalid)
            fold({
                assertEquals(3, it.size)
                assertEquals(NonEmptyList(DoesNotContain("@"), EmailMaxLength(250), UserCityInvalid(invalidUser.city)), it)
            }, {})
        }
    }

    @Test
    fun `FF on Invalid Email No needle + Invalid City`() {
        val invalidUser = User("gakshintala", "tarkansh-kt.com${(0..251).map { "g" }}", "akshintala", "tark", "vja")
        val result = nonBlockingFFValidator.validateUserWithRules(invalidUser).fix().mono.block()?.fix()
        result?.run {
            assertTrue(isLeft())
            fold({
                assertEquals(1, it.size)
                assertEquals(DoesNotContain("@"), it.head)
            }, {})
        }
    }

    @Test
    fun `FF on Invalid Email Length + Invalid Login`() {
        val invalidUser = User("tarkansh", "tarkansh@kt.com${(0..251).map { "g" }}", "akshintala", "tark", "london")
        val result = nonBlockingFFValidator.validateUserWithRules(invalidUser).fix().mono.block()?.fix()
        result?.run {
            assertTrue(isLeft())
            fold({
                assertEquals(1, it.size)
                assertEquals(EmailMaxLength(250), it.head)
            }, {})
        }
    }

    @Test
    fun `FF on Invalid City + Invalid login`() {
        val invalidUser = User("tarkansh", "tarkansh@kt.com", "akshintala", "tark", "hyd")
        val result = nonBlockingFFValidator.validateUserWithRules(invalidUser).fix().mono.block()?.fix()
        result?.run {
            assertTrue(isLeft())
            fold({
                assertEquals(1, it.size)
                assertEquals(UserCityInvalid("hyd"), it.head)
            }, {})
        }
    }

    @Test
    fun `FF on only Invalid login`() {
        val invalidUser = User("tarkansh", "tarkansh@kt.com", "akshintala", "tark", "london")
        val result = nonBlockingFFValidator.validateUserWithRules(invalidUser).fix().mono.block()?.fix()
        result?.run {
            assertTrue(isLeft())
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
