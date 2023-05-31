package gui

import freditor.Freditor
import logic.Problem
import java.awt.event.KeyEvent
import java.util.function.Consumer
import kotlin.streams.asSequence

class MainHandler : MainFlow() {
    init {
        controlPanel.randomize.addActionListener {
            initialWorld = currentProblem.randomWorld()
            atomicWorld.set(initialWorld)
            worldPanel.repaint()

            editor.requestFocusInWindow()
        }

        controlPanel.goal.addActionListener {
            atomicWorld.set(initialWorld)
            worldPanel.repaint()

            executeGoal(currentProblem.goal)
            controlPanel.stepOver.isEnabled = false
            controlPanel.stepReturn.isEnabled = false

            editor.requestFocusInWindow()
        }

        controlPanel.problemPicker.addActionListener {
            controlPanel.startStopReset.text = "start"
            controlPanel.randomize.isEnabled = currentProblem.isRandom

            initialWorld = currentProblem.randomWorld()
            atomicWorld.set(initialWorld)
            worldPanel.binaryLines = currentProblem.binaryLines
            worldPanel.repaint()

            story.load(currentProblem.story)

            val pattern = Regex("""\bvoid\s+(${currentProblem.name})\b""").toPattern()

            tabbedEditors.stream().asSequence()
                .minus(editor).plus(editor) // check current editor last
                .filter { it.setCursorTo(pattern, 1) }
                .lastOrNull()
                ?.let(tabbedEditors::selectEditor)

            editor.requestFocusInWindow()
        }

        controlPanel.startStopReset.addActionListener {
            when (controlPanel.startStopReset.text) {
                "start" -> parseAndExecute()

                "stop" -> stop()

                "reset" -> {
                    controlPanel.startStopReset.text = "start"
                    atomicWorld.set(initialWorld)
                    worldPanel.repaint()
                }
            }
            editor.requestFocusInWindow()
        }

        controlPanel.check.addActionListener {
            controlPanel.startStopReset.text = "reset"
            checkAgainst(currentProblem.goal)

            editor.requestFocusInWindow()
        }

        controlPanel.stepInto.addActionListener {
            stepInto()
        }

        controlPanel.stepOver.addActionListener {
            step { virtualMachine.stepOver() }

            editor.requestFocusInWindow()
        }

        controlPanel.stepReturn.addActionListener {
            step { virtualMachine.stepReturn() }

            editor.requestFocusInWindow()
        }

        controlPanel.slider.addChangeListener {
            val d = delay()
            if (d < 0) {
                timer.stop()
            } else {
                timer.initialDelay = d
                timer.delay = d
                if (controlPanel.isRunning()) {
                    timer.restart()
                }
            }
            editor.requestFocusInWindow()
        }

        defaultCloseOperation = EXIT_ON_CLOSE
        tabbedEditors.saveOnExit(this)
    }

    override fun createEditor(freditor: Freditor): Editor {
        val editor = Editor(freditor)

        editor.onKeyPressed { event ->
            when (event.keyCode) {
                KeyEvent.VK_M -> if (event.isControlDown && event.isShiftDown) {
                    virtualMachinePanel.isVisible = !virtualMachinePanel.isVisible
                }

                KeyEvent.VK_F12 -> {
                    if (controlPanel.isRunning()) {
                        stepInto()
                    } else {
                        controlPanel.startStopReset.doClick()
                    }
                }
            }
        }

        editor.onRightClick = Consumer { lexeme ->
            if (controlPanel.problemPicker.isEnabled) {
                Problem.problems.firstOrNull {
                    it.name == lexeme
                }?.let {
                    controlPanel.problemPicker.selectedItem = it
                }
            }
        }

        return editor
    }
}
