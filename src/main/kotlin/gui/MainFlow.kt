package gui

import common.Diagnostic
import logic.KarelError
import logic.Problem
import logic.World
import logic.execute
import syntax.lexer.Lexer
import syntax.parser.Parser
import syntax.parser.program
import vm.Emitter
import vm.Karel
import java.awt.EventQueue
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicReference
import javax.swing.Timer

open class MainFlow : MainDesign(AtomicReference(Problem.karelsFirstProgram.createWorld())), Karel.Callbacks {

    val currentProblem: Problem
        get() = controlPanel.problemPicker.selectedItem as Problem

    var initialWorld: World = atomicWorld.get()

    fun delay(): Int {
        val logarithm = controlPanel.delayLogarithm()
        return if (logarithm < 0) logarithm else 1.shl(logarithm)
    }

    val queue = ArrayBlockingQueue<Step>(1)

    val timer: Timer = Timer(delay()) {
        queue.offer(Step.INTO)
    }

    fun executeGoal() {
        execute(Problem.karel)
    }

    fun parseAndExecute() {
        editor.indent()
        editor.autosaver.save()
        editor.clearDiagnostics()
        try {
            val lexer = Lexer(editor.text)
            val parser = Parser(lexer)
            val program = parser.program()
            val main = parser.sema.command(currentProblem.name)
            if (main == null) {
                editor.setCursorTo(editor.length())
                showDiagnostic("void ${currentProblem.name}() not found")
            } else if (main.parameters.isNotEmpty()) {
                main.parameters.first().error("${currentProblem.name} cannot have parameters")
            } else {
                execute(Emitter(program).emit())
            }
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
        }
    }

    private fun start() {
        controlPanel.executionStarted()
        worldPanel.repaint()
        queue.clear()
        if (delay() >= 0) {
            timer.start()
        }
    }

    private fun stop() {
        timer.stop()
        controlPanel.executionFinished()
        editor.clearStack()
        editor.requestFocusInWindow()
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
        story.loadFromString(currentProblem.story)
        editor.requestFocusInWindow()
    }

    // Karel runs on a separate thread, thereby not blocking the UI

    private var stackDepth = 0
    private var targetStackDepth = Int.MAX_VALUE

    private fun execute(karel: Class<out Karel>) {
        start()
        Thread {
            stackDepth = 0
            targetStackDepth = Int.MAX_VALUE
            try {
                karel.execute(atomicWorld, this, currentProblem.name)
            } catch (_: StopTheProgram) {
            } catch (karelError: KarelError) {
                EventQueue.invokeLater {
                    showDiagnostic(karelError.message!!)
                }
            } catch (bug: Throwable) {
                bug.printStackTrace()
            } finally {
                EventQueue.invokeLater {
                    stop()
                }
            }
        }.start()
    }

    override fun pauseAt(position: Int) {
        if (position < 0) return

        val entry = if (stackDepth <= targetStackDepth) {
            // Step into mode
            if (position > 0) {
                EventQueue.invokeLater {
                    editor.setCursorTo(position)
                }
            }
            // Block until the next button press
            queue.take()
        } else {
            // Step over/return mode
            // Don't block, but consume potential button presses, especially stop
            queue.poll()
        }
        targetStackDepth = when (entry) {
            Step.INTO -> Int.MAX_VALUE
            Step.OVER -> stackDepth
            Step.RETURN -> stackDepth - 1
            Step.STOP -> throw StopTheProgram
            else -> targetStackDepth
        }
    }

    override fun update() {
        worldPanel.repaint()
    }

    override fun enter(callerPosition: Int, calleePosition: Int) {
        if (callerPosition < 0) return

        ++stackDepth
        EventQueue.invokeLater {
            editor.push(callerPosition, calleePosition)
        }
    }

    override fun leave() {
        if (stackDepth == 0) return

        --stackDepth
        EventQueue.invokeLater {
            editor.pop()
        }
    }
}
