package gui

import common.Stack
import common.push
import freditor.Autosaver
import freditor.FreditorUI
import freditor.JavaIndenter
import syntax.lexer.keywords

import java.awt.*
import java.awt.event.KeyEvent
import java.awt.geom.Line2D
import javax.swing.JOptionPane

private val NAME = Regex("""[A-Z_a-z][0-9A-Z_a-z]*""")
private val BUILTIN_COMMANDS = setOf("moveForward", "turnLeft", "turnAround", "turnRight", "pickBeeper", "dropBeeper")

class Editor : FreditorUI(Flexer, JavaIndenter.instance, 60, 1) {
    val autosaver: Autosaver = newAutosaver("karel")

    init {
        autosaver.loadOrDefault("""/*
F1 = moveForward();
F2 = turnLeft();
F3 = turnAround();
F4 = turnRight();
F5 = pickBeeper();
F6 = dropBeeper();
*/

void karelsFirstProgram()
{
    // your code here
    
}
""")
        listenToKeyboard()
    }

    fun insertCommand(command: String) {
        if (lineBeforeSelection.all(Char::isWhitespace)) {
            insert(command)
        } else {
            simulateEnter()
            insert(command)
            // remove the commit between simulateEnter and insertString,
            // effectively committing both changes as a single commit
            uncommit()
        }
    }

    private fun listenToKeyboard() {
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
        if (oldName.isNotEmpty() && oldName !in keywords && oldName !in BUILTIN_COMMANDS) {
            val input = JOptionPane.showInputDialog(this, oldName, "rename command", JOptionPane.QUESTION_MESSAGE, null, null, oldName)
            if (input != null) {
                val newName = input.toString()
                if (NAME.matches(newName) && newName !in keywords && newName !in BUILTIN_COMMANDS) {
                    replace("""\b$oldName(\s*\()""", "$newName$1")
                }
            }
        }
    }

    private var lines: Stack<Line2D.Double> = Stack.Nil

    private val frontHeight = FreditorUI.frontHeight
    private val frontWidth = FreditorUI.frontWidth
    private val thickness = frontWidth - 2.0f

    private val stroke = BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val color = Color(0x40ff0000, true)

    fun push(callerPosition: Int, calleePosition: Int) {
        if (callerPosition > 0) {
            val callerLine = lineOfPosition(callerPosition) + 1
            val calleeLine = lineOfPosition(calleePosition) + 1
            pushLine(callerLine, calleeLine)
            repaint()
        }
    }

    private fun pushLine(callerLine: Int, calleeLine: Int) {
        val x = 0.5 * thickness + lines.size() * frontWidth
        val y1 = (callerLine - 0.5) * frontHeight
        val y2 = (calleeLine - 0.5) * frontHeight

        val line = Line2D.Double(x, y1, x, y2)
        lines = lines.push(line)
    }

    fun pop() {
        if (!lines.isEmpty()) {
            lines = lines.pop()
            repaint()
        }
    }

    fun clearStack() {
        lines = Stack.Nil
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
        if (!lines.isEmpty()) {
            lines.forEach(graphics::draw)
            graphics.draw(lines.top())
        }
    }
}
