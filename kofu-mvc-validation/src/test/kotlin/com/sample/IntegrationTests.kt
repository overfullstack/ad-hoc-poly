package com.sample

import arrow.core.nel
import com.validation.User
import com.validation.ValidationError
import com.validation.ValidationError.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

class IntegrationTests {

    private val client = WebTestClient.bindToServer().baseUrl("http://localhost:8181").build()


    private lateinit var context: ConfigurableApplicationContext

    @BeforeAll
    fun beforeAll() {
        context = app.run(profiles = "test")
    }

	@Test
	fun `Invalid Email - Does not Contain @`() {
		val invalidEmail = "gakshintala-kt.com"
		val reasons = InvalidUser(NotAnEmail(DoesNotContain("@").nel()).nel()).nel()
		client.post().uri("/api/upsert")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.bodyValue(User("gakshintala", invalidEmail, "Gopal S", "Akshintala", "london"))
				.exchange()
				.expectStatus().isBadRequest
				.expectBody<String>().isEqualTo("Cannot Upsert!!, reasons: $reasons")
	}

    @Test
    fun `Invalid City`() {
		val invalidCity = "hyderabad"
		val reasons = InvalidUser(ValidationError.UserCityInvalid(invalidCity).nel()).nel()
		client.post().uri("/api/upsert")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(User("gakshintala", "gakshintala@kt.com", "Gopal S", "Akshintala", invalidCity))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<String>().isEqualTo("Cannot Upsert!!, reasons: $reasons")
    }

    @AfterAll
    fun afterAll() {
        context.close()
    }
}
