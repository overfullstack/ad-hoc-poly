package coroutines

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.fu.kofu.application

class CityRepositoryTests {

    private val dataApp = application {
        enable(dataConfig)
    }

    private lateinit var context: ConfigurableApplicationContext
    private lateinit var repository: CityRepository

    @BeforeAll
    fun beforeAll() {
        context = dataApp.run(profiles = "test")
        repository = context.getBean()
    }

    @Test
    fun cityNameValid() = runBlocking {
        assertTrue(repository.doesCityExistsWith("istanbul"))
    }

    @Test
    fun cityNameInValid() = runBlocking {
        assertFalse(repository.doesCityExistsWith("hyderabad"))
    }

    @AfterAll
    fun afterAll() {
        context.close()
    }
}
