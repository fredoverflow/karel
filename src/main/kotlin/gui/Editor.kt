package gui

import freditor.Freditor
import freditor.FreditorUI
import syntax.lexer.keywords
import vm.Instruction
import vm.builtinCommands
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.geom.Line2D
import javax.swing.JOptionPane

private val NAME_PARTS = Regex("""[0-9A-Z_a-z]+""")

private val COMMANDS = Regex("""\bvoid\s+([A-Z_a-z][0-9A-Z_a-z]*)\s*\(\s*\)""")

class Editor(freditor: Freditor) : FreditorUI(freditor, 60, 1) {
    init {
        onKeyPressed { event ->
            when (event.keyCode) {
                KeyEvent.VK_F1 -> insertCommand("moveForward();")
                KeyEvent.VK_F2 -> insertCommand("turnLeft();")
                KeyEvent.VK_F3 -> insertCommand("turnAround();")
                KeyEvent.VK_F4 -> insertCommand("turnRight();")
                KeyEvent.VK_F5 -> insertCommand("pickBeeper();")
                KeyEvent.VK_F6 -> insertCommand("dropBeeper();")

                KeyEvent.VK_F7 -> insert("onBeeper()")
                KeyEvent.VK_F8 -> insert("beeperAhead()")
                KeyEvent.VK_F9 -> insert("leftIsClear()")
                KeyEvent.VK_F10 -> insert("frontIsClear()")
                KeyEvent.VK_F11 -> insert("rightIsClear()")

                KeyEvent.VK_SPACE -> if (event.isControlDown) {
                    autocompleteCall()
                }

                KeyEvent.VK_R -> if (isControlRespectivelyCommandDown(event) && event.isAltDown) {
                    renameCommand()
                }
            }
        }
    }

    fun insertCommand(command: String) {
        if (lineIsBlankBefore(selectionStart())) {
            insert(command)
        } else {
            simulateEnter()
            insert(command)
            uncommit()
        }
    }

    fun insertSnippet(beforeCursor: String, beforeSelection: String, afterSelection: String) {
        if (selectionIsEmpty()) {
            if (lineIsBlankBefore(selectionStart())) {
                insert(beforeCursor, beforeSelection, afterSelection)
            } else {
                simulateEnter()
                insert(beforeCursor, beforeSelection, afterSelection)
                uncommit()
            }
        } else {
            balanceSelection()
            insert(beforeCursor, beforeSelection, afterSelection)
        }
        indent()
    }

    private fun autocompleteCall() {
        val suffixes = autocompleteCall(text, lineBeforeSelection)
        if (suffixes.size == 1) {
            insert(suffixes[0])
        } else {
            println(suffixes.sorted().joinToString(", "))
        }
    }

    private fun renameCommand() {
        val oldName = symbolNearCursor(Flexer.IDENTIFIER_TAIL)
        if (oldName.isEmpty() || oldName in keywords || oldName in builtinCommands) return

        val newName = inputCommandName("rename command") ?: return // canceled

        replace("""\b$oldName(\s*\(\s*\))""", "$newName$1")
    }

    private fun inputCommandName(title: String): String? {
        val input = JOptionPane.showInputDialog(
            this,
            "command name:",
            title,
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            null,
        ) ?: return null // canceled

        var name = NAME_PARTS.findAll(input.toString()).joinToString(separator = "_") { it.value }
        if (name.isEmpty()) {
            return "_" + System.currentTimeMillis()
        }
        if (name[0] <= '9') {
            name = "_$name"
        }
        if (name in keywords || name in builtinCommands || name in definedCommands()) {
            name += "_" + System.currentTimeMillis()
        }
        return name
    }

    fun void(currentProblemName: String) {
        val cursor = cursor()
        val target = findTopLevelFrom(cursor)
        if (target == cursor) {
            // at top level, insert here
            insertSnippet("void ", "()\n{\n    ", "\n}\n\n")
            if (currentProblemName !in definedCommands()) {
                insert(currentProblemName)
            }
        } else if (selectionIsEmpty()) {
            // insert at next top level
            setCursorTo(target)
            insert("\n\nvoid ", "()\n{\n    ", "\n}")
            indent()
            if (currentProblemName !in definedCommands()) {
                insert(currentProblemName)
            }
        } else {
            // extract selected body as command
            balanceSelection()
            val name = inputCommandName("extract command") ?: return // canceled
            val start = selectionStart()
            val body = deleteSelection()

            setCursorTo(target - body.length)
            insert("\n\nvoid $name()\n{\n$body\n}")
            setCursorTo(start)
            insert("$name();")
            indent()

            uncommit()
            uncommit()
        }
    }

    fun commandMatchGroups(): Sequence<MatchGroup> {
        return COMMANDS.findAll(text)
            .filter { stateAt(it.range.first + 3) == Flexer.VOID } // ignore comments
            .map { it.groups[1]!! }
    }

    fun definedCommands(): Sequence<String> {
        return COMMANDS.findAll(text)
            .filter { stateAt(it.range.first + 3) == Flexer.VOID } // ignore comments
            .map { it.groups[1]!!.value }
    }

    fun commandWithoutName(): Boolean {
        val i = cursor() - 2
        return stateAt(i) == Flexer.VOID &&
                stateAt(i + 1) == freditor.Flexer.SPACE_HEAD &&
                stateAt(i + 2) == freditor.Flexer.OPENING_PAREN &&
                stateAt(i + 3) == freditor.Flexer.CLOSING_PAREN
    }

    private val lines = ArrayList<Line2D.Double>()

    private val frontHeight = FreditorUI.frontHeight
    private val frontWidth = FreditorUI.frontWidth
    private val thickness = frontWidth - 2.0f

    private val stroke = BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val color = Color(0x40ff0000, true)

    fun push(callInstruction: Instruction, returnInstruction: Instruction) {
        val x = 0.5 * thickness + lines.size * frontWidth
        val y1 = (lineOfPosition(callInstruction.position) + 0.5) * frontHeight
        val y2 = (lineOfPosition(returnInstruction.position) + 0.5) * frontHeight
        lines.add(Line2D.Double(x, y1, x, y2))
        repaint()
    }

    fun pop() {
        lines.removeAt(lines.lastIndex)
        repaint()
    }

    fun clearStack() {
        lines.clear()
        repaint()
    }

    override fun paint(graphics: Graphics) {
        super.paint(graphics)
        paintCallStack(graphics as Graphics2D)
    }

    private fun paintCallStack(graphics: Graphics2D) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.stroke = stroke
        graphics.color = color
        graphics.translate(0, -firstVisibleLine() * frontHeight)
        if (lines.isNotEmpty()) {
            lines.forEach(graphics::draw)
            graphics.draw(lines.last())
        }
    }
}
