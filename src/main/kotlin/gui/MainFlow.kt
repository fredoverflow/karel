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

const val COMPARE = """ · click world to compare

right-click: goal world
 left-click: your world"""

abstract class MainFlow : MainDesign(Problem.karelsFirstProgram.randomWorld()) {

    val currentProblem: Problem
        get() = controlPanel.problemPicker.selectedItem as Problem

    fun delay(): Int {
        val logarithm = controlPanel.delayLogarithm()
        return if (logarithm < 0) logarithm else 1.shl(logarithm)
    }

    var initialWorld: World = worldPanel.world.clone()

    var virtualMachine = VirtualMachine(emptyArray(), worldPanel.world)

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

                tryCheck(instructions.toTypedArray(), createGoalInstructions(goal).toTypedArray())
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
            try {
                checkOneWorld(instructions, goalInstructions)
            } catch (error: KarelError) {
                virtualMachine.errorCurrent(error.message)
            }
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
                nextRepaint += CHECK_REPAINT_NS
            }
        }
    }

    private fun checkOneWorld(instructions: Array<Instruction>, goalInstructions: Array<Instruction>) {

        val goalMachine = createVirtualMachine(goalInstructions, currentProblem, initialWorld)
        val userMachine = createVirtualMachine(instructions, currentProblem, initialWorld)
        virtualMachine = userMachine

        while (true) {
            try {
                goalMachine.executeGoalPickDropMove()
                try {
                    userMachine.executeUserPickDropMove()

                    if (goalMachine.world.position != userMachine.world.position) {

                        worldPanel.leftWorld = userMachine.world
                        worldPanel.rightWorld = goalMachine.world
                        userMachine.errorPrevious("wrong ${currentProblem.check.singular}$COMPARE")
                    }
                } catch (_: VirtualMachine.Finished) {
                    // user machine finished early

                    if (currentProblem.dropsOptional256() && goalMachine.world.position == 1) {
                        return
                    }

                    var missing = 1
                    try {
                        while (true) {
                            goalMachine.executeGoalPickDropMove()
                            ++missing
                        }
                    } catch (_: VirtualMachine.Finished) {
                    }

                    worldPanel.leftWorld = userMachine.world
                    worldPanel.rightWorld = goalMachine.world
                    if (currentProblem.numWorlds == ONE) {
                        userMachine.errorCurrent("missing $missing ${currentProblem.check.numerus(missing)}$COMPARE")
                    } else {
                        userMachine.errorCurrent("missing ${currentProblem.check.plural}$COMPARE")
                    }
                }
            } catch (_: VirtualMachine.Finished) {
                // goal machine finished
                try {
                    userMachine.executeUserPickDropMove()

                    worldPanel.leftWorld = userMachine.world
                    worldPanel.rightWorld = goalMachine.world
                    userMachine.errorPrevious("extra ${currentProblem.check.singular}$COMPARE")
                } catch (_: VirtualMachine.Finished) {
                    // both machines finished
                    return
                }
            }
        }
    }

    private fun createVirtualMachine(
        program: Array<Instruction>,
        problem: Problem,
        world: World,
    ): VirtualMachine {
        return VirtualMachine(
            program,
            world.clone(),
            ignoreMove = (Check.EVERY_PICK_DROP == problem.check),
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

    private fun report(row: Int, message: String) {
        tabbedEditors.selectedEditor.run {
            setCursorTo(row, 25)
            insert(message)
        }
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

            val problemChunks = Problem.problems.goodChunks { parser.sema.command(it.name) != null }
            val total = problemChunks.sumOf { it.size }
            if (total == 0) return
            var passed = 0

            tabbedEditors.selectReport()
            tabbedEditors.selectedEditor.run {
                load("") // clear report
                clearDiagnostics()
            }
            report(0, problemChunks.joinToString(prefix = "START ${now()}\n\n", separator = "\n") { chunk ->
                chunk.joinToString(separator = "") { problem ->
                    "%s %-18s %n".format(problem.index, problem.name)
                }
            })
            var row = 2

            for (chunk in problemChunks) {
                for (problem in chunk) {
                    val row = row++ // capture by value
                    completion.submit {
                        val main = parser.sema.command(problem.name)!!
                        val instructions = Emitter(parser.sema, false).emit(main)
                        val goalInstructions = createGoalInstructions(problem.goal)
                        checkAllWorlds(problem, instructions.toTypedArray(), goalInstructions.toTypedArray(), row)
                    }
                }
                ++row
            }
            repeat(total) {
                completion.take().get().run {
                    if (passedWorlds == checkedWorlds) {
                        ++passed
                    }
                    val percentage = passedWorlds * 100 / checkedWorlds
                    report(this.row, "%3d%% passed (%d/%d)".format(percentage, passedWorlds, checkedWorlds))
                }
            }
            val percentage = passed * 100 / total
            report(row, "\nSTOP  ${now()}  problems %3d%% passed (%d/%d)".format(percentage, passed, total))
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
        }
    }

    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val completion = ExecutorCompletionService<Result>(executor)

    private class Result(val passedWorlds: Int, val checkedWorlds: Int, val row: Int)

    private fun checkAllWorlds(
        problem: Problem,
        instructions: Array<Instruction>,
        goalInstructions: Array<Instruction>,
        row: Int,
    ): Result {
        val start = System.nanoTime()

        val worlds = problem.randomWorlds().iterator()
        var passedWorlds = 0
        var checkedWorlds = 0

        while (true) {
            val initialWorld = worlds.next()
            try {
                if (checkWorld(problem, initialWorld, instructions, goalInstructions)) {
                    ++passedWorlds
                }
            } catch (_: KarelError) {
            }
            ++checkedWorlds

            if (!worlds.hasNext()) {
                return Result(passedWorlds, checkedWorlds, row)
            }

            val elapsed = System.nanoTime() - start

            if (elapsed >= CHECK_TOTAL_NS) {
                return Result(passedWorlds, checkedWorlds, row)
            }
        }
    }

    private fun checkWorld(
        problem: Problem,
        initialWorld: World,
        instructions: Array<Instruction>,
        goalInstructions: Array<Instruction>,
    ): Boolean {
        val goalMachine = createVirtualMachine(goalInstructions, problem, initialWorld)
        val userMachine = createVirtualMachine(instructions, problem, initialWorld)

        while (true) {
            try {
                goalMachine.executeGoalPickDropMove()
                try {
                    userMachine.executeUserPickDropMove()

                    if (goalMachine.world.position != userMachine.world.position) {
                        return false
                    }
                } catch (_: VirtualMachine.Finished) {
                    // user machine finished early
                    return problem.dropsOptional256() && goalMachine.world.position == 1
                }
            } catch (_: VirtualMachine.Finished) {
                // goal machine finished
                try {
                    userMachine.executeUserPickDropMove()
                    return false
                } catch (_: VirtualMachine.Finished) {
                    // both machines finished
                    return true
                }
            }
        }
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
            initialWorld.clone(),
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

inline fun <T> Iterable<T>.goodChunks(predicate: (T) -> Boolean): List<List<T>> {

    val outer = ArrayList<ArrayList<T>>()
    var inner = ArrayList<T>()

    for (element in this) {
        if (predicate(element)) {
            inner.add(element)
        } else if (inner.isNotEmpty()) {
            outer.add(inner)
            inner = ArrayList()
        }
    }
    if (inner.isNotEmpty()) {
        outer.add(inner)
    }
    return outer
}
