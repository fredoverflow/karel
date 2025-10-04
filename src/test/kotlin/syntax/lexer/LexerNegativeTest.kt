package syntax.lexer

import common.Diagnostic
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LexerNegativeTest {
    private fun assertDiagnostic(messageSubstring: String, input: String) {
        val lexer = Lexer(input)
        val diagnostic = assertThrows<Diagnostic> {
            lexer.nextToken()
        }
        val message = diagnostic.message
        assertTrue(message.contains(messageSubstring), message)
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

    @Test
    fun unclosedMultiLineComment() {
        assertDiagnostic(messageSubstring = "multi-line comment", input = "/*")
    }
}
