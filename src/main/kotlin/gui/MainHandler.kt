package gui

import logic.World
import logic.binaryLinesIn
import logic.goalFor
import logic.storyFor
import java.awt.event.KeyEvent

class MainHandler : MainFlow() {
    init {
        controlPanel.goal.addActionListener {
            atomicKarel.set(initialKarel)
            karelPanel.repaint()

            executeGoal(goalFor(entryPoint))
            controlPanel.stepOver.isEnabled = false
            controlPanel.stepReturn.isEnabled = false

            editor.requestFocusInWindow()
        }

        controlPanel.problemPicker.addActionListener {
            controlPanel.start_stop_reset.text = "start"

            initialKarel = World.load(currentProblem)
            atomicKarel.set(initialKarel)
            karelPanel.binaryLines = binaryLinesIn(entryPoint)
            karelPanel.repaint()

            story.loadFromString(storyFor(entryPoint))

            editor.setCursorTo("void $entryPoint()")
            editor.requestFocusInWindow()
        }

        controlPanel.start_stop_reset.addActionListener {
            when (controlPanel.start_stop_reset.text) {
                "start" ->
                    parseAndExecute()

                "stop" ->
                    stop()

                "reset" -> {
                    controlPanel.start_stop_reset.text = "start"
                    atomicKarel.set(initialKarel)
                    karelPanel.repaint()
                }
            }
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

        editor.onKeyPressed { event ->
            when (event.keyCode) {
                KeyEvent.VK_M -> if (event.isControlDown && event.isShiftDown) {
                    virtualMachinePanel.isVisible = !virtualMachinePanel.isVisible
                }
                KeyEvent.VK_F12 -> {
                    if (controlPanel.isRunning()) {
                        stepInto()
                    } else {
                        controlPanel.start_stop_reset.doClick()
                    }
                }
            }
        }

        this.onWindowClosing {
            editor.tryToSaveCode()
        }
    }
}
