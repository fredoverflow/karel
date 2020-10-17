package syntax.lexer

import common.Diagnostic
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains.containsString
import org.junit.Assert.assertThrows
import org.junit.Test

class LexerNegativeTest {
    private fun assertDiagnostic(messageSubstring: String, input: String) {
        val lexer = Lexer(input)
        val diagnostic = assertThrows(Diagnostic::class.java) {
            lexer.nextToken()
        }
        assertThat(diagnostic.message, containsString(messageSubstring))
    }

    @Test
    fun illegalCharacter() {
        assertDiagnostic(messageSubstring = "illegal character", input = "@")
    }

    @Test
    fun slashStartsComment() {
        assertDiagnostic(messageSubstring = "comments start", input = "/@")
    }

    @Test
    fun singleAmpersand() {
        assertDiagnostic(messageSubstring = "&&", input = "&")
    }

    @Test
    fun singleBar() {
        assertDiagnostic(messageSubstring = "||", input = "|")
    }
}
