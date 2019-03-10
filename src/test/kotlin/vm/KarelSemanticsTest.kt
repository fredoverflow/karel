package vm

import common.Diagnostic
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import syntax.lexer.Lexer
import syntax.parser.Parser
import syntax.parser.program

const val whileLoop = """
void main() {
    while (frontIsClear()) {
        moveForward();
    }
}
"""

const val recursion = """
void main() {
    if (frontIsClear()) {
        moveForward();
        main();
    }
}
"""

class KarelSemanticsTest {

    private fun analyze(sourceCode: String, targetLevel: Int): KarelSemantics {
        val lexer = Lexer(sourceCode)
        val parser = Parser(lexer)
        val program = parser.program()
        return KarelSemantics(program, "main", targetLevel)
    }

    private fun assertLegal(sourceCode: String, targetLevel: Int) {
        val semantics = analyze(sourceCode, targetLevel)
        val errors = semantics.errors()
        assertEquals(emptyList<Diagnostic>(), errors)
    }

    private fun assertIllegal(messageSubstring: String, sourceCode: String, targetLevel: Int) {
        val semantics = analyze(sourceCode, targetLevel)
        val errors = semantics.errors()
        if (errors.isEmpty()) {
            fail("""no errors found, but expected at least one containing "$messageSubstring"""")
        }
        for (error in errors) {
            val message = error.message
            if (!message.contains(messageSubstring)) {
                fail(""""$message" does not contain "$messageSubstring"""")
            }
        }
    }

    @Test
    fun orphanIsUnreachable() {
        val semantics = analyze("""
        void main() {
            a();
            b();
        }

        void a() {
            b();
        }

        void b() {
        }

        void c() {
        }
        """, 1)

        val commands = semantics.commands
        assertEquals(setOf("main", "a", "b", "c"), commands.keys)

        val main = commands["main"]!!
        val a = commands["a"]!!
        val b = commands["b"]!!

        val calleesOf = semantics.calleesOf
        assertEquals(listOf(a, b), calleesOf[main])
        assertEquals(listOf(b), calleesOf[a])

        val reachableCommands = semantics.reachableCommands
        assertEquals(listOf(main, a, b), reachableCommands)
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
        """, 3)
    }

    @Test
    fun undefinedCommand() {
        assertIllegal("undefined command", """
        void main() {
            a();
        }

        void b() {
        }
        """, 3)
    }

    @Test
    fun whileLoopsAreForbiddenInWeek1() {
        assertIllegal("while loop", whileLoop, 1)
    }

    @Test
    fun whileLoopsAreAllowedAfterWeek1() {
        assertLegal(whileLoop, 2)
        assertLegal(whileLoop, 3)
    }

    @Test
    fun recursionIsForbiddenBeforeWeek3() {
        assertIllegal("recursion", recursion, 1)
        assertIllegal("recursion", recursion, 2)
    }

    @Test
    fun recursionIsAllowedInWeek3() {
        assertLegal(recursion, 3)
    }

    @Test
    fun indirectRecursion() {
        assertIllegal("recursion", """
        void main() {
            a();
        }

        void a() {
            b();
        }

        void b() {
            c();
        }

        void c() {
            a();
        }
        """, 2)
    }
}
