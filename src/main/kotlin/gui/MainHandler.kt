package gui

import freditor.Freditor
import freditor.FreditorUI
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

            macroPanel.configureLevel(currentProblem.level)

            val name = currentProblem.name
            for (editor in tabbedEditors.stream().sorted(compareBy { it != editor })) {
                for (matchGroup in editor.commandMatchGroups()) {
                    if (matchGroup.value == name) {
                        tabbedEditors.selectEditor(editor)
                        editor.setCursorTo(matchGroup.range.first)
                        return@addActionListener
                    }
                }
            }

            if (editor.stateAt(editor.cursor() - 2) == Flexer.VOID
                && editor.stateAt(editor.cursor() - 1) == freditor.Flexer.SPACE_HEAD
                && editor.stateAt(editor.cursor()) == freditor.Flexer.OPENING_PAREN
                && editor.stateAt(editor.cursor() + 1) == freditor.Flexer.CLOSING_PAREN
            ) {
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

        macroPanel.undo.addActionListener {
            editor.undo()

            editor.clearDiagnostics()
            editor.requestFocusInWindow()
        }

        macroPanel.redo.addActionListener {
            editor.redo()

            editor.clearDiagnostics()
            editor.requestFocusInWindow()
        }

        macroPanel.void.addActionListener {
            editor.insertMacro("void ", "()\n{\n", "\n}\n\n")
            val name = currentProblem.name
            if (editor.commandMatchGroups().none { it.value == name }) {
                editor.insert(name)
            }
            editor.clearDiagnostics()
            editor.requestFocusInWindow()
        }

        macroPanel.commands.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(event: MouseEvent) {
                val commands = editor.commandMatchGroups()
                    .filterNot { it.value in Problem.names }
                    .mapTo(macroPanel.builtinCommands.toMutableList()) { Macro(it.value, it.value + "();") }

                val selectedItem = macroPanel.commands.selectedItem
                val model = DefaultComboBoxModel(commands.toTypedArray())
                model.selectedItem = selectedItem
                macroPanel.commands.model = model
            }
        })

        macroPanel.commands.addActionListener {
            val command = macroPanel.commands.selectedItem as Macro
            editor.insertCommand(command.code)
        }
        macroPanel.commands.addPopupMenuListener(clearAndFocus)

        macroPanel.repeats.addActionListener {
            val macro = macroPanel.repeats.selectedItem as Macro
            editor.insertMacro(macro.code, ")\n{\n", "\n}")
        }
        macroPanel.repeats.addPopupMenuListener(clearAndFocus)

        macroPanel.ifs.addActionListener {
            val macro = macroPanel.ifs.selectedItem as Macro
            editor.insertMacro("if (", ")\n{\n", macro.code)
        }
        macroPanel.ifs.addPopupMenuListener(clearAndFocus)

        macroPanel.conditions.addActionListener {
            val condition = macroPanel.conditions.selectedItem as Macro
            editor.insert(condition.code)
        }
        macroPanel.conditions.addPopupMenuListener(clearAndFocus)

        macroPanel.`while`.addActionListener {
            editor.insertMacro("while (", ")\n{\n", "\n}")

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

private val COMMANDS = Regex("""\bvoid\s+(\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*)\s*\(\s*\)""")

fun FreditorUI.commandMatchGroups(): Sequence<MatchGroup> {
    return COMMANDS.findAll(text)
        .filter { stateAt(it.range.first + 3) == Flexer.VOID } // ignore comments
        .map { it.groups[1]!! }
}
