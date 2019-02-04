package gui

import freditor.FreditorUI
import freditor.JavaIndenter
import util.Stack
import util.completeCommand
import util.push

import java.awt.*
import java.awt.event.KeyEvent
import java.awt.geom.Line2D
import java.io.File
import java.io.IOException
import java.security.MessageDigest

class Editor : FreditorUI(Flexer.instance, JavaIndenter.instance, 60, 1) {
    companion object {
        // TODO Can this be refactored to a more readable raw string without causing platform issues?
        // see https://stackoverflow.com/questions/46861701
        val firstProgram = "/*\nF1 = moveForward();\nF2 = turnLeft();\nF3 = turnAround();\nF4 = turnRight();\nF5 = pickBeeper();\nF6 = dropBeeper();\n*/\n\nvoid karelsFirstProgram()\n{\n    // your code here\n    \n}\n"

        val directory: String = "${System.getProperty("user.home")}/karel"
        val filenamePrefix: String = "$directory/karel"
        val filenameSuffix: String = ".txt"
        val filename: String = "$filenamePrefix$filenameSuffix"
    }

    init {
        tryToLoadCode()
        listenToKeyboard()
    }

    fun tryToLoadCode() {
        try {
            loadFromFile(filename)
        } catch (ignored: IOException) {
            loadFromString(firstProgram)
        }
    }

    fun tryToSaveCode() {
        createDirectory()
        tryToSaveCodeAs(filename)
        tryToSaveCodeAs(backupFilename())
    }

    private fun createDirectory() {
        if (File(directory).mkdir()) {
            println("created directory $directory")
        }
    }

    private fun tryToSaveCodeAs(pathname: String) {
        try {
            println("saving code as $pathname")
            saveToFile(pathname)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun backupFilename(): String {
        val sha1 = MessageDigest.getInstance("SHA")
        val text = text.toByteArray(Charsets.ISO_8859_1)
        val hash = sha1.digest(text)
        val builder = StringBuilder(filenamePrefix)
        builder.append('_')
        for (byte in hash) {
            val x = byte.toInt()
            builder.append("0123456789abcdef"[x.ushr(4).and(15)])
            builder.append("0123456789abcdef"[x.and(15)])
        }
        return builder.append(filenameSuffix).toString()
    }

    private fun listenToKeyboard() {
        onKeyPressed { event ->
            when (event.keyCode) {
                KeyEvent.VK_F1 -> insertString("moveForward();")
                KeyEvent.VK_F2 -> insertString("turnLeft();")
                KeyEvent.VK_F3 -> insertString("turnAround();")
                KeyEvent.VK_F4 -> insertString("turnRight();")

                KeyEvent.VK_F5 -> insertString("pickBeeper();")
                KeyEvent.VK_F6 -> insertString("dropBeeper();")
                KeyEvent.VK_F7 -> insertString("onBeeper()")
                KeyEvent.VK_F8 -> insertString("beeperAhead()")

                KeyEvent.VK_F9 -> insertString("leftIsClear()")
                KeyEvent.VK_F10 -> insertString("frontIsClear()")
                KeyEvent.VK_F11 -> insertString("rightIsClear()")

                KeyEvent.VK_SPACE -> if (event.isControlDown) {
                    val suffixes = completeCommand(text, lineUntilCursor)
                    when (suffixes.size) {
                        0 -> {
                        }

                        1 -> insertString(suffixes[0])

                        else -> println(suffixes.joinToString(", "))
                    }
                }
            }
        }
    }

    private var lines: Stack<Line2D.Double> = Stack.Nil

    private val fontHeight = FreditorUI.fontHeight
    private val fontWidth = FreditorUI.fontWidth
    private val thickness = fontWidth - 2.0f

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
        val x = 0.5 * thickness + lines.size() * fontWidth
        val y1 = (callerLine - 0.5) * fontHeight
        val y2 = (calleeLine - 0.5) * fontHeight

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
        graphics.translate(0, -firstVisibleLine() * fontHeight)
        if (!lines.isEmpty()) {
            lines.forEach(graphics::draw)
            graphics.draw(lines.top())
        }
    }
}
