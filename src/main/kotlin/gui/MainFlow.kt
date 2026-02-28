package gui

import common.Diagnostic
import logic.*
import syntax.lexer.Lexer
import syntax.parser.Parser
import syntax.parser.program
import vm.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
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
        tabbedEditors.selectNonReport()
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
                tryCheck(instructions.toTypedArray(), goalInstructions.toTypedArray())
            } else {
                editor.setCursorTo(editor.length())
                showDiagnostic("void ${currentProblem.name}() not found")
            }
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
        }
    }

    private fun tryCheck(instructions: Array<Instruction>, goalInstructions: Array<Instruction>) {
        try {
            val successMessage = check(instructions, goalInstructions)
            reportFirstRedundantCondition(instructions)
            update()
            showDiagnostic(successMessage)
        } catch (diagnostic: Diagnostic) {
            update()
            showDiagnostic(diagnostic)
        }
    }

    private fun check(instructions: Array<Instruction>, goalInstructions: Array<Instruction>): String {

        val start = System.nanoTime()
        var nextRepaint = CHECK_REPAINT_NS

        val worlds = currentProblem.randomWorlds().iterator()
        var worldCounter = 0

        while (true) {
            initialWorld = worlds.next()
            checkOneWorld(instructions, goalInstructions)
            ++worldCounter

            if (!worlds.hasNext()) {
                return if (currentProblem.numWorlds == ONE) {
                    "OK: every ${currentProblem.check.singular} matches the goal :-)"
                } else {
                    "OK: checked all ${currentProblem.numWorlds} possible worlds :-)"
                }
            }

            val elapsed = System.nanoTime() - start

            if (elapsed >= CHECK_TOTAL_NS) {
                return if (currentProblem.numWorlds == UNKNOWN) {
                    "OK: checked $worldCounter random worlds :-)"
                } else {
                    "OK: checked $worldCounter random worlds :-)\n    from ${currentProblem.numWorlds} possible worlds"
                }
            }

            if (elapsed >= nextRepaint) {
                worldPanel.world = initialWorld
                worldPanel.paintImmediately(0, 0, worldPanel.width, worldPanel.height)
                nextRepaint += CHECK_REPAINT_NS
            }
        }
    }

    private var index = 0

    private fun checkOneWorld(instructions: Array<Instruction>, goalInstructions: Array<Instruction>) {
        val goalWorlds = ArrayList<World>(200)
        virtualMachine = createVirtualMachine(goalInstructions, initialWorld, goalWorlds::add)
        try {
            virtualMachine.executeGoalProgram()
        } catch (_: VirtualMachine.Finished) {
        }
        val finalGoalWorld = virtualMachine.world
        index = 0

        virtualMachine = createVirtualMachine(instructions, initialWorld) { world ->
            if (index == goalWorlds.size) {
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
        if (index < goalWorlds.size && !finalGoalWorld.equalsIgnoringDirection(virtualMachine.world)) {
            worldPanel.antWorld = finalGoalWorld
            if (currentProblem.numWorlds == ONE) {
                val missing = goalWorlds.size - index
                virtualMachine.error("missing $missing ${currentProblem.check.numerus(missing)}\n\n$COMPARE")
            } else {
                virtualMachine.error("missing ${currentProblem.check.plural}\n\n$COMPARE")
            }
        }
    }

    private fun createVirtualMachine(
        program: Array<Instruction>,
        world: World,
        callback: (World) -> Unit,
    ): VirtualMachine {
        return VirtualMachine(
            program,
            world,
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

    private fun now(): String {
        return LocalTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_TIME)
    }

    private fun report(message: String) {
        tabbedEditors.selectedEditor.insert(message)
        tabbedEditors.tabs.run {
            paintImmediately(0, 0, width, height)
        }
    }

    fun checkAllProblems() {
        tabbedEditors.selectNonReport()
        editor.isolateBraces()
        editor.indent()
        editor.saveWithBackup()
        editor.clearDiagnostics()
        try {
            val lexer = Lexer(editor.text)
            val parser = Parser(lexer)
            parser.program()

            val problems = Problem.problems.filter { parser.sema.command(it.name) != null }
            val total = problems.size
            if (total == 0) return
            var passed = 0

            tabbedEditors.selectReport()
            tabbedEditors.selectedEditor.run {
                load("") // clear report
                clearDiagnostics()
            }
            report("${now()} START checking $total problems\n\n")

            for (problem in problems) {
                val main = parser.sema.command(problem.name)!!
                val instructions = Emitter(parser.sema, false).emit(main)
                val goalInstructions = createGoalInstructions(problem.goal)
                completion.submit {
                    checkAllWorlds(problem, instructions.toTypedArray(), goalInstructions.toTypedArray())
                }
            }
            repeat(total) {
                val result = completion.take().get()
                report(result.message)
                if (result.pass) {
                    ++passed
                }
            }
            val percentage = passed * 100 / total
            report("\n${now()} FINISHED, %3d%% passed (%d/%d)%n".format(percentage, passed, total))
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
        }
    }

    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val completion = ExecutorCompletionService<Result>(executor)

    private class Result(val pass: Boolean, val message: String)

    private fun checkAllWorlds(
        problem: Problem,
        instructions: Array<Instruction>,
        goalInstructions: Array<Instruction>,
    ): Result {
        val start = System.nanoTime()

        val worlds = problem.randomWorlds().iterator()
        var worldCounter = 0
        var passed = 0

        while (true) {
            val initialWorld = worlds.next()
            if (checkWorld(initialWorld, instructions, goalInstructions)) {
                ++passed
            }
            ++worldCounter

            if (!worlds.hasNext()) {
                val percentage = passed * 100 / worldCounter
                return Result(
                    passed == worldCounter,
                    "%-18s %3d%% passed (%d/%d)%n".format(problem.name, percentage, passed, worldCounter),
                )
            }

            val elapsed = System.nanoTime() - start

            if (elapsed >= CHECK_TOTAL_NS) {
                val percentage = passed * 100 / worldCounter
                return Result(
                    passed == worldCounter,
                    "%-18s %3d%% passed (%d/%d) TIMEOUT%n".format(problem.name, percentage, passed, worldCounter),
                )
            }
        }
    }

    private fun checkWorld(
        initialWorld: World,
        instructions: Array<Instruction>,
        goalInstructions: Array<Instruction>,
    ): Boolean {
        val goalWorlds = ArrayList<World>(200)
        var virtualMachine = createVirtualMachine(goalInstructions, initialWorld, goalWorlds::add)
        try {
            virtualMachine.executeGoalProgram()
        } catch (_: VirtualMachine.Finished) {
        }
        val finalGoalWorld = virtualMachine.world
        var index = 0

        virtualMachine = createVirtualMachine(instructions, initialWorld) { world ->
            if (index == goalWorlds.size) {
                throw Weltschmerz // caught below to return false
            }
            val goalWorld = goalWorlds[index++]
            if (!goalWorld.equalsIgnoringDirection(world)) {
                throw Weltschmerz // caught below to return false
            }
        }

        try {
            virtualMachine.executeUserProgram()
        } catch (_: VirtualMachine.Finished) {
        } catch (_: KarelError) {
            return false
        }
        return !(index < goalWorlds.size && !finalGoalWorld.equalsIgnoringDirection(virtualMachine.world))
    }

    fun parseAndExecute() {
        tabbedEditors.selectNonReport()
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
            instructions.toTypedArray(),
            initialWorld,
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
            editor.insert(
                """
void karelsFirstProgram()
{
    // your code here
    
}

"""
            )
            editor.uncommit()
            editor.setCursorTo(editor.length() - 4)
        }
        editor.requestFocusInWindow()
    }
}
