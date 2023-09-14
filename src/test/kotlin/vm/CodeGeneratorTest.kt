package vm

import common.subList
import org.junit.Assert.assertEquals
import org.junit.Test
import syntax.lexer.Lexer
import syntax.parser.Parser
import syntax.parser.program

class CodeGeneratorTest {
    private fun compile(sourceCode: String): List<Instruction> {
        val lexer = Lexer(sourceCode)
        val parser = Parser(lexer)
        val program = parser.program()
        val main = program.commands.first()
        return CodeGenerator(parser.sema).generate(main)
    }

    private fun assertBytecode(sourceCode: String, vararg bytecodes: Int) {
        val expected = bytecodes.map { bytecode -> "%x".format(bytecode) }
        val actual = compile(sourceCode).subList(ENTRY_POINT).map { instruction -> "%x".format(instruction.bytecode) }
        assertEquals(expected, actual)
    }

    @Test
    fun basicCommands() {
        assertBytecode(
            """
            void main() {
                moveForward();
                turnLeft();
                turnAround();
                turnRight();
                pickBeeper();
                dropBeeper();
            }
            """,
            MOVE_FORWARD,
            TURN_LEFT,
            TURN_AROUND,
            TURN_RIGHT,
            PICK_BEEPER,
            DROP_BEEPER,
            RETURN,
        )
    }

    @Test
    fun call() {
        assertBytecode(
            """
            void main() {
                moveForward();
                turns();
                moveForward();
                beepers();
                moveForward();
            }

            void turns() {
                turnLeft();
                turnAround();
                turnRight();
            }

            void beepers() {
                pickBeeper();
                dropBeeper();
            }
            """,
            MOVE_FORWARD,
            CALL + 0x106,
            MOVE_FORWARD,
            CALL + 0x10a,
            MOVE_FORWARD,
            RETURN,

            TURN_LEFT,
            TURN_AROUND,
            TURN_RIGHT,
            RETURN,

            PICK_BEEPER,
            DROP_BEEPER,
            RETURN,
        )
    }

    @Test
    fun repeat() {
        assertBytecode(
            """
            void main() {
                repeat (9) {
                    moveForward();
                }
            }
            """,
            PUSH + 9,
            MOVE_FORWARD,
            LOOP + 0x101,
            RETURN,
        )
    }

    @Test
    fun nestedRepeat() {
        assertBytecode(
            """
            void main() {
                repeat (4) {
                    repeat (9) {
                        moveForward();
                    }
                }
            }
            """,
            PUSH + 4,
            PUSH + 9,
            MOVE_FORWARD,
            LOOP + 0x102,
            LOOP + 0x101,
            RETURN
        )
    }

    @Test
    fun ifThenTrue() {
        assertBytecode(
            """
            void main() {
                if (onBeeper()) {
                    pickBeeper();
                }
            }
            """,
            ON_BEEPER, ELSE + 0x103,
            PICK_BEEPER,
            RETURN,
        )
    }

    @Test
    fun ifThenFalse() {
        assertBytecode(
            """
            void main() {
                if (!onBeeper()) {
                    dropBeeper();
                }
            }
            """,
            ON_BEEPER, THEN + 0x103,
            DROP_BEEPER,
            RETURN,
        )
    }

    @Test
    fun ifElseTrue() {
        assertBytecode(
            """
            void main() {
                if (onBeeper()) {
                    pickBeeper();
                } else {
                    dropBeeper();
                }
            }
            """,
            ON_BEEPER, ELSE + 0x104,
            PICK_BEEPER,
            JUMP + 0x105,
            DROP_BEEPER,
            RETURN,
        )
    }

    @Test
    fun ifElseFalse() {
        assertBytecode(
            """
            void main() {
                if (!onBeeper()) {
                    dropBeeper();
                } else {
                    pickBeeper();
                }
            }
            """,
            ON_BEEPER, THEN + 0x104,
            DROP_BEEPER,
            JUMP + 0x105,
            PICK_BEEPER,
            RETURN,
        )
    }

    @Test
    fun and() {
        assertBytecode(
            """
            void main() {
                if (!leftIsClear() && !frontIsClear() && !rightIsClear()) {
                    turnAround();
                }
            }
            """,
            LEFT_IS_CLEAR, NOT,
            FRONT_IS_CLEAR, NOT,
            RIGHT_IS_CLEAR, NOT,
            AND, AND,
            ELSE + 0x10a,
            TURN_AROUND,
            RETURN,
        )
    }

    @Test
    fun or() {
        assertBytecode(
            """
            void main() {
                if (!(leftIsClear() || frontIsClear() || rightIsClear())) {
                    turnAround();
                }
            }
            """,
            LEFT_IS_CLEAR, FRONT_IS_CLEAR, RIGHT_IS_CLEAR,
            OR, OR, THEN + 0x107,
            TURN_AROUND,
            RETURN,
        )
    }

    @Test
    fun elseIf() {
        assertBytecode(
            """
            void main() {
                if (leftIsClear()) {
                    turnLeft();
                } else if (frontIsClear()) {
                } else if (rightIsClear()) {
                    turnRight();
                } else {
                    turnAround();
                }
            }
            """,
            LEFT_IS_CLEAR, ELSE + 0x104,
            TURN_LEFT,
            JUMP + 0x10c,
            FRONT_IS_CLEAR, ELSE + 0x107,
            JUMP + 0x10c,
            RIGHT_IS_CLEAR, ELSE + 0x10b,
            TURN_RIGHT,
            JUMP + 0x10c,
            TURN_AROUND,
            RETURN,
        )
    }

    @Test
    fun whi1e() {
        assertBytecode(
            """
            void hangTheLampions() {
                while (beeperAhead()) {
                    moveForward();
                    pickBeeper();
                }
            }
            """,
            BEEPER_AHEAD, ELSE + 0x105,
            MOVE_FORWARD,
            PICK_BEEPER,
            JUMP + 0x100,
            RETURN,
        )
    }

    @Test
    fun recursion() {
        assertBytecode(
            """
            void partyAgain() {
                if (!frontIsClear()) {
                    turnAround();
                } else {
                    moveForward();
                    partyAgain();
                    moveForward();
                }
            }
            """,
            FRONT_IS_CLEAR, THEN + 0x104,
            TURN_AROUND,
            JUMP + 0x107,
            MOVE_FORWARD,
            CALL + 0x100,
            MOVE_FORWARD,
            RETURN,
        )
    }

    @Test
    fun oddNumberOfNegations() {
        assertBytecode(
            """
            void main() {
                if (!!!onBeeper()) {
                    dropBeeper();
                }
            }
            """,
            ON_BEEPER, THEN + 0x103,
            DROP_BEEPER,
            RETURN,
        )
    }

    @Test
    fun evenNumberOfNegations() {
        assertBytecode(
            """
            void main() {
                if (!!!!frontIsClear()) {
                    moveForward();
                }
            }
            """,
            FRONT_IS_CLEAR, ELSE + 0x103,
            MOVE_FORWARD,
            RETURN,
        )
    }

    @Test
    fun infiniteLoop() {
        assertBytecode(
            """
            void hangTheLampions() {
                while (true) {
                    turnLeft();
                }
            }
            """,
            TRUE,
            ELSE + 0x104,
            TURN_LEFT,
            JUMP + 0x100,
            RETURN,
        )
    }

    @Test
    fun disabledViaIf() {
        assertBytecode(
            """
            void main() {
                if (false) {
                    turnAround();
                    moveForward();
                    turnAround();
                }
            }
            """,
            FALSE,
            ELSE + 0x105,
            TURN_AROUND,
            MOVE_FORWARD,
            TURN_AROUND,
            RETURN,
        )
    }
}
