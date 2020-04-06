package coroutines

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.WebApplicationType
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.fu.kofu.application

class UserRepositoryTests {

    private val dataApp = application(WebApplicationType.NONE) {
        enable(dataConfig)
    }

    private lateinit var context: ConfigurableApplicationContext
    private lateinit var repository: UserRepository

    @BeforeAll
    fun beforeAll() {
        context = dataApp.run(profiles = "test")
        repository = context.getBean()
    }

    @Test
    fun count() = runBlocking {
        assertEquals(3, repository.count())
    }

    @Test
    fun userLoginValid() = runBlocking {
        assertTrue(repository.doesUserExistsWith("tarkansh"))
    }

    @Test
    fun userLoginInValid() = runBlocking {
        assertFalse(repository.doesUserExistsWith("gakshintala"))
    }

    @AfterAll
    fun afterAll() {
        context.close()
    }
}
