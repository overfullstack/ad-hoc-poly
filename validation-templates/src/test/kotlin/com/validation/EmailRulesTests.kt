/* gakshintala created on 3/16/20 */
package com.validation

import arrow.core.fix
import com.validation.rules.validateEmailWithRules
import com.validation.typeclass.failFast
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EmailRulesTests {

    @Test
    fun `FF Email Rule Runner on Invalid user`() {
        val invalidUser = User("tarkansh", "tarkansh-kt.com${(0..251).map { "g" }}", "akshintala", "tark", "london")
        val result = failFast<ValidationError>().validateEmailWithRules(invalidUser.email).fix()
        Assertions.assertTrue(result.isLeft() ?: false)
        result.fold({
            Assertions.assertEquals(1, it.size)
            Assertions.assertEquals(ValidationError.DoesNotContain("@"), it.head)
        }, {})
    }
}
