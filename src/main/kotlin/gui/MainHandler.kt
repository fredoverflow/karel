package gui

import freditor.Freditor
import logic.Problem
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.function.Consumer
import javax.swing.SwingUtilities
import kotlin.streams.asSequence

class MainHandler : MainFlow() {
    init {
        controlPanel.randomize.addActionListener {
            controlPanel.startStopReset.text = "start"

            initialWorld = currentProblem.randomWorld()
            virtualMachine.world = initialWorld
            worldPanel.world = initialWorld
            worldPanel.antWorld = null
            worldPanel.repaint()

            editor.requestFocusInWindow()
        }

        controlPanel.goal.addActionListener {
            worldPanel.antWorld = null

            executeGoal(currentProblem.goal)
            controlPanel.stepOver.isEnabled = false
            controlPanel.stepReturn.isEnabled = false

            editor.requestFocusInWindow()
        }

        controlPanel.problemPicker.addActionListener {
            controlPanel.startStopReset.text = "start"
            controlPanel.randomize.isEnabled = currentProblem.isRandom
            controlPanel.check.toolTipText = "check every ${currentProblem.check.singular}"

            initialWorld = currentProblem.randomWorld()
            virtualMachine.world = initialWorld
            worldPanel.world = initialWorld
            worldPanel.antWorld = null
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

                    virtualMachine.world = initialWorld
                    worldPanel.world = initialWorld
                    worldPanel.antWorld = null
                    worldPanel.repaint()
                }
            }
            editor.requestFocusInWindow()
        }

        controlPanel.check.addActionListener {
            controlPanel.startStopReset.text = "reset"
            worldPanel.antWorld = null

            checkAgainst(currentProblem.goal)

            editor.requestFocusInWindow()
        }

        controlPanel.stepInto.addActionListener {
            tryStep(::stepInto)

            editor.requestFocusInWindow()
        }

        controlPanel.stepOver.addActionListener {
            tryStep(virtualMachine::stepOver)

            editor.requestFocusInWindow()
        }

        controlPanel.stepReturn.addActionListener {
            tryStep(virtualMachine::stepReturn)

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

        var previousValue = controlPanel.slider.value

        controlPanel.pause.addActionListener {
            with(controlPanel.slider) {
                if (value != minimum) {
                    if (value != maximum) {
                        previousValue = value
                    }
                    value = minimum
                } else {
                    value = previousValue
                }
            }
        }

        controlPanel.fast.addActionListener {
            with(controlPanel.slider) {
                if (value != maximum) {
                    if (value != minimum) {
                        previousValue = value
                    }
                    value = maximum
                } else {
                    value = previousValue
                }
            }
        }

        worldPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                if (!event.component.isEnabled) return

                if (SwingUtilities.isLeftMouseButton(event)) {
                    if (worldPanel.antWorld == null) {
                        val x = event.x / worldPanel.tileSize
                        val y = event.y / worldPanel.tileSize
                        val world = virtualMachine.world.toggleBeeper(x, y)
                        virtualMachine.world = world

                        worldPanel.world = world
                        worldPanel.repaint()
                    }
                }
            }
        })

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
                        tryStep(::stepInto)
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
