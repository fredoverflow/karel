package gui

import common.Diagnostic
import common.Stack
import logic.*
import syntax.lexer.Lexer
import syntax.parser.Parser
import syntax.parser.program
import vm.CodeGenerator
import vm.Instruction
import vm.VirtualMachine
import java.awt.EventQueue
import javax.swing.Timer

const val CHECK_TOTAL_NS = 2_000_000_000L
const val CHECK_REPAINT_NS = 100_000_000L

const val COMPARE = "mouse enter/exit (or click) world to compare"

abstract class MainFlow : MainDesign(WorldRef(Problem.karelsFirstProgram.randomWorld())),
    VirtualMachine.Callbacks {

    val currentProblem: Problem
        get() = controlPanel.problemPicker.selectedItem as Problem

    fun delay(): Int {
        val logarithm = controlPanel.delayLogarithm()
        return if (logarithm < 0) logarithm else 1.shl(logarithm)
    }

    var initialWorld: World = worldRef.world

    lateinit var virtualMachine: VirtualMachine

    val timer: Timer = Timer(delay()) {
        step { virtualMachine.stepInto(virtualMachinePanel.isVisible) }
    }

    fun executeGoal(goal: String) {
        start(vm.createGoalInstructions(goal))
    }

    fun checkAgainst(goal: String) {
        editor.indent()
        editor.saveWithBackup()
        editor.clearDiagnostics()
        try {
            val lexer = Lexer(editor.text)
            val parser = Parser(lexer)
            parser.program()
            val main = parser.sema.command(currentProblem.name)
            if (main != null) {
                val instructions: List<Instruction> = CodeGenerator(parser.sema).generate(main)
                virtualMachinePanel.setProgram(instructions)

                val goalInstructions = vm.createGoalInstructions(goal)

                check(instructions, goalInstructions)
            } else {
                editor.setCursorTo(editor.length())
                showDiagnostic("void ${currentProblem.name}() not found")
            }
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
        }
    }

    private fun check(instructions: List<Instruction>, goalInstructions: List<Instruction>) {
        controlPanel.checkStarted()
        worldPanel.isEnabled = false

        fun cleanup() {
            controlPanel.checkFinished(currentProblem.isRandom)
            worldPanel.isEnabled = true

            virtualMachinePanel.clearStack()
            editor.clearStack()
            update()
        }

        val start = System.nanoTime()
        var nextRepaint = CHECK_REPAINT_NS

        val worlds = currentProblem.randomWorlds().iterator()
        var worldCounter = 0

        fun checkBetweenRepaints() {
            try {
                while (worlds.hasNext()) {
                    initialWorld = worlds.next()
                    checkOneWorld(instructions, goalInstructions)
                    ++worldCounter

                    val elapsed = System.nanoTime() - start
                    if (elapsed >= CHECK_TOTAL_NS) {
                        cleanup()
                        if (currentProblem.numWorlds == UNKNOWN) {
                            showDiagnostic("OK: checked $worldCounter random worlds")
                        } else {
                            showDiagnostic("OK: checked $worldCounter random worlds\n    from ${currentProblem.numWorlds} possible worlds")
                        }
                        return
                    } else if (elapsed >= nextRepaint) {
                        worldRef.world = initialWorld
                        worldPanel.repaint()
                        nextRepaint += CHECK_REPAINT_NS
                        EventQueue.invokeLater(::checkBetweenRepaints)
                        return
                    }
                }
                cleanup()
                showDiagnostic("OK: checked all ${currentProblem.numWorlds} possible worlds")
            } catch (diagnostic: Diagnostic) {
                cleanup()
                showDiagnostic(diagnostic)
            }
        }

        checkBetweenRepaints()
    }

    private fun checkOneWorld(instructions: List<Instruction>, goalInstructions: List<Instruction>) {
        val goalWorlds = goalWorlds(goalInstructions)
        val goalWorldIterator = goalWorlds.iterator()

        worldRef.world = initialWorld
        createVirtualMachine(instructions) { world ->
            if (!goalWorldIterator.hasNext()) {
                worldPanel.antWorld = goalWorlds.last()
                throw Diagnostic(virtualMachine.currentInstruction.position, "overshoots goal\n$COMPARE")
            }
            val goalWorld = goalWorldIterator.next()
            if (!goalWorld.equalsIgnoringDirection(world)) {
                worldPanel.antWorld = goalWorld
                throw Diagnostic(virtualMachine.currentInstruction.position, "deviates from goal\n$COMPARE")
            }
        }

        try {
            virtualMachine.executeUserProgram()
        } catch (_: Stack.Exhausted) {
        } catch (error: KarelError) {
            throw Diagnostic(virtualMachine.currentInstruction.position, error.message!!)
        }
        if (goalWorldIterator.hasNext() && !goalWorlds.last().equalsIgnoringDirection(virtualMachine.world)) {
            worldPanel.antWorld = goalWorldIterator.next()
            throw Diagnostic(virtualMachine.currentInstruction.position, "falls short of goal\n$COMPARE")
        }
    }

    private fun createVirtualMachine(instructions: List<Instruction>, callback: (World) -> Unit) {
        virtualMachine = when (currentProblem.check) {
            Check.EVERY_PICK_DROP_MOVE ->
                VirtualMachine(instructions, worldRef, ignoreCallAndReturn, callback, callback)

            Check.EVERY_PICK_DROP ->
                VirtualMachine(instructions, worldRef, ignoreCallAndReturn, callback)
        }
    }

    private fun goalWorlds(goalInstructions: List<Instruction>): List<World> {
        val goalWorlds = ArrayList<World>(200)
        worldRef.world = initialWorld
        createVirtualMachine(goalInstructions, goalWorlds::add)
        try {
            virtualMachine.executeGoalProgram()
        } catch (_: Stack.Exhausted) {
        }
        return goalWorlds
    }

    private val ignoreCallAndReturn = object : VirtualMachine.Callbacks {
        override fun onInfiniteLoop() {
            throw Diagnostic(virtualMachine.currentInstruction.position, "infinite loop detected")
        }
    }

    fun parseAndExecute() {
        editor.indent()
        editor.saveWithBackup()
        editor.clearDiagnostics()
        try {
            val lexer = Lexer(editor.text)
            val parser = Parser(lexer)
            parser.program()
            val main = parser.sema.command(currentProblem.name)
            if (main != null) {
                val instructions = CodeGenerator(parser.sema).generate(main)
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
        tabbedEditors.tabs.isEnabled = false
        virtualMachinePanel.setProgram(instructions)
        virtualMachine = VirtualMachine(instructions, worldRef, this)
        controlPanel.executionStarted()
        update()
        if (delay() >= 0) {
            timer.start()
        }
    }

    fun stop() {
        timer.stop()
        controlPanel.executionFinished(currentProblem.isRandom)
        virtualMachinePanel.clearStack()
        tabbedEditors.tabs.isEnabled = true
        editor.clearStack()
        editor.requestFocusInWindow()
    }

    override fun onCall(callerPosition: Int, calleePosition: Int) {
        editor.push(callerPosition, calleePosition)
    }

    override fun onReturn() {
        editor.pop()
    }

    override fun onInfiniteLoop() {
        showDiagnostic("infinite loop detected")
    }

    fun update() {
        val instruction = virtualMachine.currentInstruction
        val position = instruction.position
        if (position > 0) {
            editor.setCursorTo(position)
        }
        virtualMachinePanel.update(virtualMachine.pc, virtualMachine.stack)
        worldPanel.repaint()
    }

    fun stepInto() {
        step { virtualMachine.stepInto(virtualMachinePanel.isVisible) }
        editor.requestFocusInWindow()
    }

    fun step(how: () -> Unit) {
        try {
            how()
            update()
        } catch (_: Stack.Exhausted) {
            stop()
            update()
        } catch (error: KarelError) {
            stop()
            update()
            showDiagnostic(error.message!!)
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
