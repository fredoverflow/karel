package gui

import common.Diagnostic
import logic.*
import syntax.lexer.Lexer
import syntax.parser.Parser
import syntax.parser.program
import vm.*
import java.awt.EventQueue
import javax.swing.Timer

const val CHECK_TOTAL_NS = 2_000_000_000L
const val CHECK_REPAINT_NS = 100_000_000L

const val COMPARE = "mouse enter/exit (or click) world to compare"

abstract class MainFlow : MainDesign(Problem.karelsFirstProgram.randomWorld()) {

    val currentProblem: Problem
        get() = controlPanel.problemPicker.selectedItem as Problem

    fun delay(): Int {
        val logarithm = controlPanel.delayLogarithm()
        return if (logarithm < 0) logarithm else 1.shl(logarithm)
    }

    var initialWorld: World = worldPanel.world

    var virtualMachine = VirtualMachine(emptyArray(), initialWorld)

    val timer = Timer(delay()) {
        tryStep(::stepInto)
    }

    fun executeGoal(goal: String) {
        start(createGoalInstructions(goal))
    }

    fun checkAgainst(goal: String) {
        editor.isolateBraces()
        editor.indent()
        editor.saveWithBackup()
        editor.clearDiagnostics()
        try {
            val lexer = Lexer(editor.text)
            val parser = Parser(lexer)
            parser.program()
            val main = parser.sema.command(currentProblem.name)
            if (main != null) {
                val instructions = Emitter(parser.sema, true).emit(main)
                virtualMachinePanel.setProgram(instructions)
                virtualMachinePanel.update(null, ENTRY_POINT)

                val goalInstructions = createGoalInstructions(goal)

                check(instructions.toTypedArray(), goalInstructions.toTypedArray())
            } else {
                editor.setCursorTo(editor.length())
                showDiagnostic("void ${currentProblem.name}() not found")
            }
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
        }
    }

    private fun check(instructions: Array<Instruction>, goalInstructions: Array<Instruction>) {

        controlPanel.checkStarted()
        worldPanel.isEnabled = false
        tabbedEditors.tabs.isEnabled = false

        fun cleanup() {
            controlPanel.checkFinished(currentProblem.isRandom)
            worldPanel.isEnabled = true
            tabbedEditors.tabs.isEnabled = true

            editor.clearStack()
            update()
        }

        val start = System.nanoTime()
        var nextRepaint = CHECK_REPAINT_NS

        val worlds = currentProblem.randomWorlds().iterator()
        var worldCounter = 0

        fun checkBetweenRepaints() {
            try {
                while (true) {
                    initialWorld = worlds.next()
                    checkOneWorld(instructions, goalInstructions)
                    ++worldCounter

                    if (!worlds.hasNext()) {
                        cleanup()
                        reportFirstRedundantCondition(instructions)

                        if (currentProblem.numWorlds == ONE) {
                            showDiagnostic("OK: every ${currentProblem.check.singular} matches the goal :-)")
                        } else {
                            showDiagnostic("OK: checked all ${currentProblem.numWorlds} possible worlds :-)")
                        }
                        return
                    }

                    val elapsed = System.nanoTime() - start

                    if (elapsed >= CHECK_TOTAL_NS) {
                        cleanup()
                        reportFirstRedundantCondition(instructions)

                        if (currentProblem.numWorlds == UNKNOWN) {
                            showDiagnostic("OK: checked $worldCounter random worlds :-)")
                        } else {
                            showDiagnostic("OK: checked $worldCounter random worlds :-)\n    from ${currentProblem.numWorlds} possible worlds")
                        }
                        return
                    }

                    if (elapsed >= nextRepaint) {
                        worldPanel.world = initialWorld
                        worldPanel.repaint()
                        nextRepaint += CHECK_REPAINT_NS
                        EventQueue.invokeLater(::checkBetweenRepaints)
                        return
                    }
                }
            } catch (diagnostic: Diagnostic) {
                cleanup()
                showDiagnostic(diagnostic)
            }
        }

        checkBetweenRepaints()
    }

    private fun checkOneWorld(instructions: Array<Instruction>, goalInstructions: Array<Instruction>) {
        val goalWorlds = ArrayList<World>(200)
        createVirtualMachine(goalInstructions, goalWorlds::add)
        try {
            virtualMachine.executeGoalProgram()
        } catch (_: VirtualMachine.Finished) {
        }
        val finalGoalWorld = virtualMachine.world
        var index = 0
        val size = goalWorlds.size

        createVirtualMachine(instructions) { world ->
            if (index == size) {
                worldPanel.antWorld = finalGoalWorld
                virtualMachine.error("extra ${currentProblem.check.singular}\n\n$COMPARE")
            }
            val goalWorld = goalWorlds[index++]
            if (!goalWorld.equalsIgnoringDirection(world)) {
                worldPanel.antWorld = goalWorld
                virtualMachine.error("wrong ${currentProblem.check.singular}\n\n$COMPARE")
            }
        }

        try {
            virtualMachine.executeUserProgram()
        } catch (_: VirtualMachine.Finished) {
        } catch (error: KarelError) {
            virtualMachine.error(error.message)
        }
        if (index < size && !finalGoalWorld.equalsIgnoringDirection(virtualMachine.world)) {
            worldPanel.antWorld = finalGoalWorld
            if (currentProblem.numWorlds == ONE) {
                val missing = size - index
                virtualMachine.error("missing $missing ${currentProblem.check.numerus(missing)}\n\n$COMPARE")
            } else {
                virtualMachine.error("missing ${currentProblem.check.plural}\n\n$COMPARE")
            }
        }
    }

    private fun createVirtualMachine(instructions: Array<Instruction>, callback: (World) -> Unit) {
        virtualMachine = VirtualMachine(
            instructions, initialWorld,
            onPickDrop = callback,
            onMove = callback.takeIf { Check.EVERY_PICK_DROP_MOVE == currentProblem.check },
        )
    }

    private fun reportFirstRedundantCondition(instructions: Array<Instruction>) {
        for (index in ENTRY_POINT until instructions.size) {
            val instruction = instructions[index]
            when (instruction.bytecode) {
                ON_BEEPER_FALSE, BEEPER_AHEAD_FALSE, LEFT_IS_CLEAR_FALSE, FRONT_IS_CLEAR_FALSE, RIGHT_IS_CLEAR_FALSE -> {
                    instruction.error("condition was always false")
                }

                ON_BEEPER_TRUE, BEEPER_AHEAD_TRUE, LEFT_IS_CLEAR_TRUE, FRONT_IS_CLEAR_TRUE, RIGHT_IS_CLEAR_TRUE -> {
                    instruction.error("condition was always true")
                }
            }
        }
    }

    fun parseAndExecute() {
        editor.isolateBraces()
        editor.indent()
        editor.saveWithBackup()
        editor.clearDiagnostics()
        try {
            val lexer = Lexer(editor.text)
            val parser = Parser(lexer)
            parser.program()
            val main = parser.sema.command(currentProblem.name)
            if (main != null) {
                val instructions = Emitter(parser.sema, false).emit(main)
                start(instructions)
            } else {
                editor.setCursorTo(editor.length())
                showDiagnostic("void ${currentProblem.name}() not found")
            }
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
        }
    }

    fun start(instructions: List<Instruction>) {
        val compiledFromSource = instructions[ENTRY_POINT].compiledFromSource
        tabbedEditors.tabs.isEnabled = false
        virtualMachinePanel.setProgram(instructions)
        virtualMachine = VirtualMachine(
            instructions.toTypedArray(), initialWorld,
            onCall = editor::push.takeIf { compiledFromSource },
            onReturn = editor::pop.takeIf { compiledFromSource },
        )
        controlPanel.executionStarted()
        update()
        if (delay() >= 0) {
            timer.start()
        }
    }

    fun stop() {
        timer.stop()
        controlPanel.executionFinished(currentProblem.isRandom)
        tabbedEditors.tabs.isEnabled = true
        editor.clearStack()
        editor.requestFocusInWindow()
    }

    fun update() {
        val instruction = virtualMachine.currentInstruction
        val position = instruction.position
        if (position > 0) {
            editor.setCursorTo(position)
        }
        virtualMachinePanel.update(virtualMachine.stack, virtualMachine.pc)
        worldPanel.world = virtualMachine.world
        worldPanel.repaint()
    }

    fun stepInto() {
        virtualMachine.stepInto(virtualMachinePanel.isVisible)
    }

    inline fun tryStep(step: () -> Unit) {
        try {
            step()
            update()
        } catch (_: VirtualMachine.Finished) {
            stop()
            update()
        } catch (error: KarelError) {
            stop()
            update()
            showDiagnostic(error.message)
        }
    }

    fun showDiagnostic(diagnostic: Diagnostic) {
        editor.setCursorTo(diagnostic.position)
        showDiagnostic(diagnostic.message)
    }

    fun showDiagnostic(message: String) {
        editor.requestFocusInWindow()
        editor.showDiagnostic(message)
    }

    init {
        story.load(currentProblem.story)
        if (editor.length() == 0) {
            editor.load(helloWorld)
        }
        editor.requestFocusInWindow()
    }
}

const val helloWorld = """/*
F1 = moveForward();
F2 = turnLeft();
F3 = turnAround();
F4 = turnRight();
F5 = pickBeeper();
F6 = dropBeeper();
*/

void karelsFirstProgram()
{
    // your code here
    
}
"""
