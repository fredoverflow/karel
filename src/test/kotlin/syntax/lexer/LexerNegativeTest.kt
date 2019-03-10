package syntax.lexer

import common.Diagnostic
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class LexerNegativeTest {
    @Rule
    @JvmField
    val thrown: ExpectedException = ExpectedException.none()

    private fun assertDiagnostic(messageSubstring: String, input: String) {
        val lexer = Lexer(input)
        thrown.expect(Diagnostic::class.java)
        thrown.expectMessage(messageSubstring)
        lexer.nextToken()
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
