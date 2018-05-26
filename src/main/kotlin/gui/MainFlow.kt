package gui

import logic.KarelError
import logic.KarelWorld
import logic.World
import logic.storyFor
import parsing.Diagnostic
import parsing.KarelSemantics
import parsing.Lexer
import parsing.Parser
import vm.CodeGenerator
import vm.Instruction
import vm.VirtualMachine
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicReference
import javax.swing.Timer

open class MainFlow : MainDesign(AtomicReference(World.karelsFirstProgram)) {

    val currentIndex: Int
        get() = controlPanel.problemPicker.selectedIndex

    val currentProblem: Method
        get() = World.problemMethods[currentIndex]

    val entryPoint: String
        get() = World.problemNames[currentIndex]

    val currentLevel: Int
        get() = controlPanel.problemPicker.getItemAt(currentIndex)[0] - '0'

    fun delay(): Int {
        val logarithm = controlPanel.delayLogarithm()
        return if (logarithm < 0) logarithm else 1.shl(logarithm)
    }

    var initialKarel: KarelWorld = atomicKarel.get()

    lateinit var virtualMachine: VirtualMachine

    val timer: Timer = Timer(delay()) {
        step { virtualMachine.stepInto(virtualMachinePanel.isVisible) }
    }

    fun executeGoal(goal: String) {
        val instructions = vm.instructionBuffer()
        instructions.addAll(goal.map { vm.goalInstruction(it.toInt()) })
        start(instructions)
    }

    fun parseAndExecute() {
        processProgram { semantics ->
            if (semantics.commands.contains(entryPoint)) {
                val instructions = CodeGenerator(semantics).generate()
                start(instructions)
            } else {
                showErrorDialog("undefined command $entryPoint", "Missing entry point")
            }
        }
    }

    fun start(instructions: List<Instruction>) {
        virtualMachinePanel.setProgram(instructions)
        virtualMachine = VirtualMachine(instructions, atomicKarel, editor::push, editor::pop, this::infiniteLoopDetected)
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
        karelPanel.repaint()
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

            val semantics = KarelSemantics(program, entryPoint, currentLevel)
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
        story.loadFromString(storyFor(entryPoint))
        editor.requestFocusInWindow()
    }
}
