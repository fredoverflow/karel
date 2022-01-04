package vm

import common.Diagnostic
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import syntax.lexer.Lexer
import syntax.parser.Parser
import syntax.parser.program

class KarelSemanticsTest {

    private fun analyze(sourceCode: String): KarelSemantics {
        val lexer = Lexer(sourceCode)
        val parser = Parser(lexer)
        val program = parser.program()
        return KarelSemantics(program)
    }

    private fun assertLegal(sourceCode: String) {
        val semantics = analyze(sourceCode)
        val errors = semantics.errors()
        assertEquals(emptyList<Diagnostic>(), errors)
    }

    private fun assertIllegal(messageSubstring: String, sourceCode: String) {
        val semantics = analyze(sourceCode)
        val errors = semantics.errors()
        assertFalse(errors.isEmpty())
        for (error in errors) {
            assertThat(error.message, containsString(messageSubstring))
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
        """)

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
    fun whileLoopsAreForbiddenInWeek1() {
        assertIllegal("while loop", """
        void karelsFirstProgram() {
            moveToWall();
        }

        void moveToWall() {
            while (frontIsClear()) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun whileLoopsAreAllowedInWeek2() {
        assertLegal("""
        void hangTheLampions() {
            moveToWall();
        }

        void moveToWall() {
            while (frontIsClear()) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun whileLoopsAreAllowedInWeek3() {
        assertLegal("""
        void partyAgain() {
            moveToWall();
        }

        void moveToWall() {
            while (frontIsClear()) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun recursionIsForbiddenInWeek1() {
        assertIllegal("recursion", """
        void karelsFirstProgram() {
            moveToWall();
        }

        void moveToWall() {
            if (frontIsClear()) {
                moveForward();
                moveToWall();
            }
        }
        """)
    }

    @Test
    fun recursionIsForbiddenInWeek2() {
        assertIllegal("recursion", """
        void hangTheLampions() {
            moveToWall();
        }

        void moveToWall() {
            if (frontIsClear()) {
                moveForward();
                moveToWall();
            }
        }
        """)
    }

    @Test
    fun recursionIsAllowedInWeek3() {
        assertLegal("""
        void partyAgain() {
            moveToWall();
        }

        void moveToWall() {
            if (frontIsClear()) {
                moveForward();
                moveToWall();
            }
        }
        """)
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
        """)
    }
}
