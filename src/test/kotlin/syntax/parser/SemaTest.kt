package syntax.parser

import common.Diagnostic
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import syntax.lexer.Lexer

class SemaTest {
    private fun assertIllegal(messageSubstring: String, sourceCode: String) {
        try {
            val lexer = Lexer(sourceCode)
            val parser = Parser(lexer)
            parser.program()
            fail()
        } catch (diagnostic: Diagnostic) {
            val message = diagnostic.message
            assertTrue(message.contains(messageSubstring), message)
        }
    }

    @Test
    fun duplicateCommand() {
        assertIllegal(
            "duplicate", """
        void main() {
            pickBeeper();
        }

        void main() {
            dropBeeper();
        }
        """
        )
    }

    @Test
    fun redefineBuiltin() {
        assertIllegal(
            "redefine builtin", """
        void turnRight() {
            turnLeft();
            turnLeft();
            turnLeft();
        }
        """
        )
    }

    @Test
    fun undefinedCommand() {
        assertIllegal(
            "Did you mean b?", """
        void main() {
            a();
        }

        void b() {
        }
        """
        )
    }
}
