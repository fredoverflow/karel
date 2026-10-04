package vm

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import syntax.lexer.Lexer
import syntax.parser.Parser
import syntax.parser.program

class EmitterTest {
    private fun assertBytecode(sourceCode: String, vararg bytecode: Byte) {
        val lexer = Lexer(sourceCode)
        val parser = Parser(lexer)
        val program = parser.program()
        val main = program.commands.first()
        val instructions = Emitter(parser.sema, false).emit(main)
        assertArrayEquals(bytecode, instructions.toBytecode())
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
            CALL, 0x06,
            MOVE_FORWARD,
            CALL, 0x0a,
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
            PUSH, 9,
            MOVE_FORWARD,
            LOOP, 0x01,
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
            PUSH, 4,
            PUSH, 9,
            MOVE_FORWARD,
            LOOP, 0x02,
            LOOP, 0x01,
            RETURN,
        )
    }

    @Test
    fun between() {
        assertBytecode(
            """
            void main() {
                repeat (7) {
                    moveForward();
                } between {
                    pickBeeper();
                }
            }
            """,
            PUSH, 7,
            JUMP, 0x03,
            PICK_BEEPER,
            MOVE_FORWARD,
            LOOP, 0x02,
            RETURN,
        )
    }

    @Test
    fun nestedBetween() {
        assertBytecode(
            """
            void main() {
                repeat (2) {
                    repeat (7) {
                        moveForward();
                    } between {
                        pickBeeper();
                    }
                } between {
                    turnLeft();
                    moveForward();
                    turnLeft();
                }
            }
            """,
            PUSH, 2,
            JUMP, 0x05,
            TURN_LEFT,
            MOVE_FORWARD,
            TURN_LEFT,
            PUSH, 7,
            JUMP, 0x08,
            PICK_BEEPER,
            MOVE_FORWARD,
            LOOP, 0x07,
            LOOP, 0x02,
            RETURN,
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
            ON_BEEPER, ELSE, 0x03,
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
            ON_BEEPER, THEN, 0x03,
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
            ON_BEEPER, ELSE, 0x04,
            PICK_BEEPER,
            JUMP, 0x05,
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
            ON_BEEPER, THEN, 0x04,
            DROP_BEEPER,
            JUMP, 0x05,
            PICK_BEEPER,
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
            LEFT_IS_CLEAR, ELSE, 0x04,
            TURN_LEFT,
            JUMP, 0x0c,
            FRONT_IS_CLEAR, ELSE, 0x07,
            JUMP, 0x0c,
            RIGHT_IS_CLEAR, ELSE, 0x0b,
            TURN_RIGHT,
            JUMP, 0x0c,
            TURN_AROUND,
            RETURN,
        )
    }

    @Test
    fun obstacle1() {
        assertBytecode(
            """
            void main() {
                if (!frontIsClear() || beeperAhead()) {
                    turnLeft();
                }
            }
            """,
            FRONT_IS_CLEAR, ELSE, 0x04,
            BEEPER_AHEAD, ELSE, 0x05,
            TURN_LEFT,
            RETURN,
        )
    }

    @Test
    fun obstacle2() {
        assertBytecode(
            """
            void main() {
                if (beeperAhead() || !frontIsClear()) {
                    turnLeft();
                }
            }
            """,
            BEEPER_AHEAD, THEN, 0x04,
            FRONT_IS_CLEAR, THEN, 0x05,
            TURN_LEFT,
            RETURN,
        )
    }

    @Test
    fun shot1() {
        assertBytecode(
            """
            void main() {
                if (!onBeeper() && frontIsClear()) {
                    moveForward();
                }
            }
            """,
            ON_BEEPER, THEN, 0x05,
            FRONT_IS_CLEAR, ELSE, 0x05,
            MOVE_FORWARD,
            RETURN,
        )
    }

    @Test
    fun shot2() {
        assertBytecode(
            """
            void main() {
                if (frontIsClear() && !onBeeper()) {
                    moveForward();
                }
            }
            """,
            FRONT_IS_CLEAR, ELSE, 0x05,
            ON_BEEPER, THEN, 0x05,
            MOVE_FORWARD,
            RETURN,
        )
    }

    @Test
    fun deadEnd1() {
        assertBytecode(
            """
            void main() {
                if (!leftIsClear() && !frontIsClear() && !rightIsClear()) {
                    turnAround();
                }
            }
            """,
            LEFT_IS_CLEAR, THEN, 0x07,
            FRONT_IS_CLEAR, THEN, 0x07,
            RIGHT_IS_CLEAR, THEN, 0x07,
            TURN_AROUND,
            RETURN,
        )
    }

    @Test
    fun deadEnd2() {
        assertBytecode(
            """
            void main() {
                if (!(leftIsClear() || frontIsClear() || rightIsClear())) {
                    turnAround();
                }
            }
            """,
            LEFT_IS_CLEAR, THEN, 0x07,
            FRONT_IS_CLEAR, THEN, 0x07,
            RIGHT_IS_CLEAR, THEN, 0x07,
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
            JUMP, 0x03,
            MOVE_FORWARD,
            PICK_BEEPER,
            BEEPER_AHEAD, THEN, 0x01,
            RETURN,
        )
    }

    @Test
    fun nestedWhile() {
        assertBytecode(
            """
            void main() {
                while (onBeeper()) {
                    pickBeeper();
                    while (frontIsClear()) {
                        moveForward();
                    }
                    turnLeft();
                }
            }
            """,
            JUMP, 0x07,
            PICK_BEEPER,
            JUMP, 0x04,
            MOVE_FORWARD,
            FRONT_IS_CLEAR, THEN, 0x03,
            TURN_LEFT,
            ON_BEEPER,
            THEN, 0x01,
            RETURN,
        )
    }

    @Test
    fun callWhile() {
        assertBytecode(
            """
            void main() {
                moveToWall();
            }
            
            void moveToWall() {
                while (frontIsClear()) {
                    moveForward();
                }
            }
            """,
            CALL, 0x04,
            RETURN,
            JUMP, 0x04,
            MOVE_FORWARD,
            FRONT_IS_CLEAR, THEN, 0x03,
            RETURN,
        )
    }

    @Test
    fun ifWhile() {
        assertBytecode(
            """
            void main() {
                if (onBeeper()) {
                    pickBeeper();
                } else {
                    while (frontIsClear()) {
                        moveForward();
                    }
                }
                while (frontIsClear()) {
                    moveForward();
                }
            }
            """,
            ON_BEEPER,
            ELSE, 0x06,
            PICK_BEEPER,
            JUMP, 0x0a,
            JUMP, 0x06,
            MOVE_FORWARD,
            FRONT_IS_CLEAR, THEN, 0x05,
            JUMP, 0x0a,
            MOVE_FORWARD,
            FRONT_IS_CLEAR, THEN, 0x09,
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
            FRONT_IS_CLEAR, THEN, 0x04,
            TURN_AROUND,
            JUMP, 0x07,
            MOVE_FORWARD,
            CALL, 0x00,
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
            ON_BEEPER, THEN, 0x03,
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
            FRONT_IS_CLEAR, ELSE, 0x03,
            MOVE_FORWARD,
            RETURN,
        )
    }

    @Test
    fun infiniteRecursion2() {
        assertBytecode(
            """
            void f() { g(); }
            void g() { f(); }
            """,
            CALL, 0x02, RETURN,
            CALL, 0x00, RETURN,
        )
    }

    @Test
    fun infiniteRecursion3() {
        assertBytecode(
            """
            void f() { g(); }
            void g() { h(); }
            void h() { f(); }
            """,
            CALL, 0x02, RETURN,
            CALL, 0x04, RETURN,
            CALL, 0x00, RETURN,
        )
    }
}
