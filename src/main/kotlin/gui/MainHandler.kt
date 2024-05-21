package gui

import java.awt.event.KeyEvent

class MainHandler : MainFlow() {
    init {
        controlPanel.goal.addActionListener {
            atomicWorld.set(initialWorld)
            worldPanel.repaint()

            executeGoal()
            controlPanel.stepOver.isEnabled = false
            controlPanel.stepReturn.isEnabled = false

            editor.requestFocusInWindow()
        }

        controlPanel.problemPicker.addActionListener {
            controlPanel.startStopReset.text = "start"

            initialWorld = currentProblem.createWorld()
            atomicWorld.set(initialWorld)
            worldPanel.binaryLines = currentProblem.binaryLines
            worldPanel.repaint()

            story.loadFromString(currentProblem.story)

            editor.setCursorTo("""\bvoid\s+(${currentProblem.name})\b""", 1)
            editor.requestFocusInWindow()
        }

        controlPanel.startStopReset.addActionListener {
            when (controlPanel.startStopReset.text) {
                "start" -> {
                    parseAndExecute()
                }
                "stop" -> {
                    queue.put(Step.STOP)
                }
                "reset" -> {
                    controlPanel.startStopReset.text = "start"
                    atomicWorld.set(initialWorld)
                    worldPanel.repaint()
                }
            }
            editor.requestFocusInWindow()
        }

        controlPanel.stepInto.addActionListener {
            queue.offer(Step.INTO)
            editor.requestFocusInWindow()
        }

        controlPanel.stepOver.addActionListener {
            queue.offer(Step.OVER)
            editor.requestFocusInWindow()
        }

        controlPanel.stepReturn.addActionListener {
            queue.offer(Step.RETURN)
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
                KeyEvent.VK_F12 -> {
                    if (controlPanel.isRunning()) {
                        queue.offer(Step.INTO)
                    } else {
                        controlPanel.startStopReset.doClick()
                    }
                }
            }
        }

        defaultCloseOperation = EXIT_ON_CLOSE

        this.onWindowClosing {
            editor.autosaver.save()
        }
    }
}
