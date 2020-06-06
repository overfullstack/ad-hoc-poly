/* gakshintala created on 3/16/20 */
package top

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.fix
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.typeclass.failFast
import top.typeclass.validateEmailWithRules

class EmailRulesTests {

    @Test
    fun `FF Email Rule Runner on Invalid user`() {
        val invalidUser = User("tarkansh", "tarkansh-kt.com${(0..251).map { "g" }}", "akshintala", "tark", "london")
        val result: Either<NonEmptyList<ValidationError>, String> = failFast<ValidationError>().validateEmailWithRules(invalidUser.email).fix()
        Assertions.assertTrue(result.isLeft())
        result.fold({
            Assertions.assertEquals(1, it.size)
            Assertions.assertEquals(ValidationError.DoesNotContain("@"), it.head)
        }, {})
    }
}
