package gui

import freditor.Freditor
import logic.Problem
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.function.Consumer
import javax.swing.DefaultComboBoxModel
import javax.swing.SwingUtilities
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener

class MainHandler : MainFlow() {
    init {
        val clearAndFocus = object : PopupMenuListener {
            override fun popupMenuWillBecomeVisible(event: PopupMenuEvent) {
            }

            override fun popupMenuWillBecomeInvisible(event: PopupMenuEvent) {
                editor.clearDiagnostics()
                editor.requestFocusInWindow()
            }

            override fun popupMenuCanceled(event: PopupMenuEvent) {
            }
        }

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

            snippetPanel.configureLevel(currentProblem.level)

            val name = currentProblem.name
            for (editor in tabbedEditors.stream().sorted(compareBy { it != editor })) {
                for (matchGroup in (editor as Editor).commandMatchGroups()) {
                    if (matchGroup.value == name) {
                        tabbedEditors.selectEditor(editor)
                        editor.setCursorTo(matchGroup.range.first)
                        return@addActionListener
                    }
                }
            }

            if (editor.commandWithoutName()) {
                editor.insert(name)
            }
        }
        controlPanel.problemPicker.addPopupMenuListener(clearAndFocus)

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

        snippetPanel.undo.addActionListener {
            editor.undo()

            editor.clearDiagnostics()
            editor.requestFocusInWindow()
        }

        snippetPanel.redo.addActionListener {
            editor.redo()

            editor.clearDiagnostics()
            editor.requestFocusInWindow()
        }

        snippetPanel.void.addActionListener {
            editor.void(currentProblem.name)

            editor.clearDiagnostics()
            editor.requestFocusInWindow()
        }

        snippetPanel.commands.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(event: MouseEvent) {
                val commands = editor.definedCommands()
                    .filter { it !in Problem.names }
                    .mapTo(snippetPanel.builtinCommands.toMutableList()) { Snippet(it, "$it();") }

                val selectedItem = snippetPanel.commands.selectedItem
                val model = DefaultComboBoxModel(commands.toTypedArray())
                model.selectedItem = selectedItem
                snippetPanel.commands.model = model
            }
        })

        snippetPanel.commands.addActionListener {
            val command = snippetPanel.commands.selectedItem as Snippet
            editor.insertCommand(command.code)
        }
        snippetPanel.commands.addPopupMenuListener(clearAndFocus)

        snippetPanel.repeats.addActionListener {
            val snippet = snippetPanel.repeats.selectedItem as Snippet
            editor.insertSnippet(snippet.code, ")\n{\n", "\n}")
        }
        snippetPanel.repeats.addPopupMenuListener(clearAndFocus)

        snippetPanel.ifs.addActionListener {
            val snippet = snippetPanel.ifs.selectedItem as Snippet
            editor.insertSnippet("if (", ")\n{\n", snippet.code)
        }
        snippetPanel.ifs.addPopupMenuListener(clearAndFocus)

        snippetPanel.conditions.addActionListener {
            val condition = snippetPanel.conditions.selectedItem as Snippet
            editor.insert(condition.code)
        }
        snippetPanel.conditions.addPopupMenuListener(clearAndFocus)

        snippetPanel.`while`.addActionListener {
            editor.insertSnippet("while (", ")\n{\n", "\n}")

            editor.clearDiagnostics()
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
