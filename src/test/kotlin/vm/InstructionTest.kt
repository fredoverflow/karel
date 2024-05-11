package vm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val GOAL = 0
private const val HUMAN = 1

class InstructionTest {

    private fun assertPause(bytecode: Int, address: Int) {
        assertTrue(Instruction(bytecode, address).shouldPause())
    }

    private fun assertNoPause(bytecode: Int, address: Int) {
        assertFalse(Instruction(bytecode, address).shouldPause())
    }

    @Test
    fun commandsAlwaysPause() {
        for (bytecode in MOVE_FORWARD..DROP_BEEPER) {
            assertPause(bytecode, GOAL)
            assertPause(bytecode, HUMAN)
        }
        assertPause(MOVE_BACKWARD, GOAL)
        assertPause(MOVE_BACKWARD, HUMAN)
    }

    @Test
    fun goalConditionsNeverPause() {
        for (bytecode in ON_BEEPER..RIGHT_IS_CLEAR) {
            assertNoPause(bytecode, GOAL)
        }
    }

    @Test
    fun humanConditionsAlwaysPause() {
        for (bytecode in ON_BEEPER..RIGHT_IS_CLEAR) {
            assertPause(bytecode, HUMAN)
        }
    }

    @Test
    fun otherGoalInstructionsNeverPause() {
        assertNoPause(RETURN, GOAL)
        assertNoPause(PUSH, GOAL)
        assertNoPause(LOOP, GOAL)
        assertNoPause(CALL, GOAL)
        assertNoPause(JUMP, GOAL)
        assertNoPause(ELSE, GOAL)
        assertNoPause(THEN, GOAL)
    }

    @Test
    fun otherHumanInstructionsAlwaysPauseExceptBranches() {
        assertPause(RETURN, HUMAN)
        assertPause(PUSH, HUMAN)
        assertPause(LOOP, HUMAN)
        assertPause(CALL, HUMAN)
        assertNoPause(JUMP, HUMAN)
        assertNoPause(ELSE, HUMAN)
        assertNoPause(THEN, HUMAN)
    }
}
