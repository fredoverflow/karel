package parsing

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class ParserNegativeTest {
    @Rule
    @JvmField
    val thrown: ExpectedException = ExpectedException.none()

    private fun assertDiagnostic(messageSubstring: String, sourceCode: String) {
        val lexer = Lexer(sourceCode)
        val parser = Parser(lexer)
        thrown.expect(Diagnostic::class.java)
        thrown.expectMessage(messageSubstring)
        parser.program()
    }

    @Test
    fun tooManyClosingBraces() {
        assertDiagnostic("Too many closing braces", """
        void main() {
          }
        }
        """)
    }

    @Test
    fun commandMissingVoid() {
        assertDiagnostic("missing void", """
        main() {
        }
        """)
    }

    @Test
    fun commandMissingName() {
        assertDiagnostic("missing identifier", """
        void () {
        }
        """)
    }

    @Test
    fun commandMissingParameters() {
        assertDiagnostic("missing (", """
        void main {
        }
        """)
    }

    @Test
    fun commandMissingBody() {
        assertDiagnostic("missing {", """
        void main()
        """)
    }

    @Test
    fun nestedCommands() {
        assertDiagnostic("nested", """
        void outer() {
            void inner() {
            }
        }
        """)
    }

    @Test
    fun unclosedBlock() {
        assertDiagnostic("unclosed block", """
        void main() {
            if (onBeeper()) {
                pickBeeper();
        }
        """)
    }

    @Test
    fun numbersAreNotStatements() {
        assertDiagnostic("Illegal start of statement", """
        void main() {
            123
        }
        """)
    }

    @Test
    fun commandMissingArguments() {
        assertDiagnostic("missing (", """
        void main() {
            other;
        }
        """)
    }

    @Test
    fun commandMissingSemicolon() {
        assertDiagnostic("missing ;", """
        void main() {
            other()
        }
        """)
    }

    @Test
    fun repeatMissingParens() {
        assertDiagnostic("missing (", """
        void main() {
            repeat 9 {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun repeatMissingBlock() {
        assertDiagnostic("missing {", """
        void main() {
            repeat (9)
                moveForward();
        }
        """)
    }

    @Test
    fun zeroRepetitions() {
        assertDiagnostic("0 < 2", """
        void main() {
            repeat (0) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun oneRepetition() {
        assertDiagnostic("1 < 2", """
        void main() {
            repeat (1) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun tooManyRepetitions() {
        assertDiagnostic("4096 > 4095", """
        void main() {
            repeat (4096) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun integerOverflow() {
        assertDiagnostic("2147483648 > 4095", """
        void main() {
            repeat (2147483648) {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun ifMissingParens() {
        assertDiagnostic("missing (", """
        void main() {
            if frontIsClear() {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun ifMissingBlock() {
        assertDiagnostic("missing {", """
        void main() {
            if (frontIsClear())
                moveForward();
        }
        """)
    }

    @Test
    fun elseRequiresBlockOrIf() {
        assertDiagnostic("{ or if", """
        void main() {
            if (onBeeper()) {
                pickBeeper();
            }
            else dropBeeper();
        }
        """)
    }

    @Test
    fun whileMissingParens() {
        assertDiagnostic("missing (", """
        void main() {
            while frontIsClear() {
                moveForward();
            }
        }
        """)
    }

    @Test
    fun whileMissingBlock() {
        assertDiagnostic("missing {", """
        void main() {
            while (frontIsClear())
                moveForward();
        }
        """)
    }

    @Test
    fun statementAsCondition() {
        assertDiagnostic("Illegal start of condition", """
        void main() {
            if (turnAround()) {
            }
        }
        """)
    }

    @Test
    fun conditionMissingParens() {
        assertDiagnostic("missing (", """
        void main() {
            while (frontIsClear) {
                moveForward();
            }
        }
        """)
    }
}
