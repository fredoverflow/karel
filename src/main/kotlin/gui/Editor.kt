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

private val NAME = Regex("""[A-Z_a-z][0-9A-Z_a-z]*""")

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

    private fun insertCommand(command: String) {
        if (lineBeforeSelection.all(Char::isWhitespace)) {
            insert(command)
        } else {
            simulateEnter()
            insert(command)
            // Remove the commit between simulateEnter and insertString,
            // effectively committing both changes as a single commit
            uncommit()
        }
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

        val input = JOptionPane.showInputDialog(
            this,
            oldName,
            "rename command",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            oldName
        ) ?: return

        val newName = input.toString().trim()
        if (!NAME.matches(newName) || newName in keywords || newName in builtinCommands) return

        replace("""\b$oldName(\s*\(\s*\))""", "$newName$1")
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
        lines.removeLast()
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
