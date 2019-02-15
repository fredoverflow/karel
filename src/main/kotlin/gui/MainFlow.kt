package gui

import logic.KarelError
import logic.Problem
import logic.World
import parsing.*
import vm.CodeGenerator
import vm.Instruction
import vm.VirtualMachine

import java.util.concurrent.atomic.AtomicReference

import javax.swing.Timer

open class MainFlow : MainDesign(AtomicReference(Problem.karelsFirstProgram.createWorld())) {

    val currentProblem: Problem
        get() = controlPanel.problemPicker.selectedItem as Problem

    fun delay(): Int {
        val logarithm = controlPanel.delayLogarithm()
        return if (logarithm < 0) logarithm else 1.shl(logarithm)
    }

    var initialWorld: World = atomicWorld.get()

    lateinit var virtualMachine: VirtualMachine

    val timer: Timer = Timer(delay()) {
        step { virtualMachine.stepInto(virtualMachinePanel.isVisible) }
    }

    fun executeGoal(goal: String) {
        val instructions = vm.createInstructionBuffer()
        instructions.addAll(goal.map { vm.goalInstruction(it.toInt()) })
        start(instructions)
    }

    fun parseAndExecute() {
        processProgram { semantics ->
            if (semantics.commands.contains(currentProblem.name)) {
                val instructions = CodeGenerator(semantics).generate()
                start(instructions)
            } else {
                showErrorDialog("undefined command ${currentProblem.name}", "Missing entry point")
            }
        }
    }

    fun start(instructions: List<Instruction>) {
        virtualMachinePanel.setProgram(instructions)
        virtualMachine = VirtualMachine(instructions, atomicWorld, editor::push, editor::pop, this::infiniteLoopDetected)
        controlPanel.executionStarted()
        update()
        if (delay() >= 0) {
            timer.start()
        }
    }

    fun stop() {
        timer.stop()
        controlPanel.executionFinished()
        virtualMachinePanel.clearStack()
        editor.clearStack()
        editor.requestFocusInWindow()
    }

    fun infiniteLoopDetected() {
        showErrorDialog("Please check your program for infinite loops!", "Timeout expired")
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
        } catch (error: AssertionError) {
            // TODO Does Kotlin have exception filters/guards?
            if (!error.message!!.contains("empty")) throw error
            // The final RET instruction tried to pop off the empty stack.
            stop()
            update()
        } catch (error: KarelError) {
            stop()
            update()
            showErrorDialog(error.message!!, "Runtime Error")
        }
    }

    fun processProgram(how: (KarelSemantics) -> Unit) {
        editor.tryToSaveCode()
        try {
            val lexer = Lexer(editor.text)
            val parser = Parser(lexer)
            val program = parser.program()

            val semantics = KarelSemantics(program, currentProblem.name, currentProblem.level)
            val errors = semantics.errors()
            if (errors.isEmpty()) {
                how(semantics)
            } else {
                showDiagnostic(errors[0], "Semantic Error")
            }
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic, "Syntax Error")
        }
    }

    fun showDiagnostic(diagnostic: Diagnostic, title: String) {
        editor.setCursorTo(diagnostic.position)
        editor.requestFocus()
        showErrorDialog(diagnostic.message, title)
    }

    init {
        story.loadFromString(currentProblem.story)
        editor.requestFocusInWindow()
    }
}
