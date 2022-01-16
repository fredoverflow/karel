package syntax.parser

import common.Diagnostic
import org.junit.Assert.*
import org.junit.Test
import syntax.lexer.Lexer

class SemaTest {
    private fun assertIllegal(messageSubstring: String, sourceCode: String) {
        try {
            val lexer = Lexer(sourceCode)
            val parser = Parser(lexer)
            parser.program()
            fail()
        } catch (diagnostic: Diagnostic) {
            if (!diagnostic.message.contains(messageSubstring)) {
                fail(diagnostic.message)
            }
        }
    }

    @Test
    fun duplicateCommand() {
        assertIllegal("duplicate", """
        void main() {
            pickBeeper();
        }

        void main() {
            dropBeeper();
        }
        """)
    }

    @Test
    fun redefineBuiltin() {
        assertIllegal("duplicate", """
        void turnRight() {
            turnLeft();
            turnLeft();
            turnLeft();
        }
        """)
    }

    @Test
    fun undefinedCommand() {
        assertIllegal("Did you mean b?", """
        void main() {
            a();
        }

        void b() {
        }
        """)
    }

    @Test
    fun tooManyArguments() {
        assertIllegal("takes 0 arguments, not 1", """
        void main() {
            first(moveForward);
        }
        
        void first() {
        }
        """)
    }

    @Test
    fun tooFewArguments() {
        assertIllegal("takes 1 arguments, not 0", """
        void main() {
            second();
        }
        
        void second(void f()) {
        }
        """)
    }

    @Test
    fun thirdOrder() {
        assertIllegal("order", """
        void main() {
            second(second);
        }
        
        void second(void f()) {
        }
        """)
    }

    @Test
    fun shadowing1() {
        assertIllegal("takes 0 arguments, not 1", """
        void f(void f()) {
            f(f);
        }
        """)
    }

    @Test
    fun shadowing2() {
        assertIllegal("takes 1 arguments, not 0", """
        void f(void f()) {
        }
        
        void main() {
            f();
        }
        """)
    }
}
