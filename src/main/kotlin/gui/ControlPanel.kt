package gui

import logic.Problem

import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JSlider

class ControlPanel(problems: List<Problem>) : VerticalBoxPanel() {

    val goal = JButton("goal")

    val problemPicker = JComboBox(problems.toTypedArray()).apply {
        maximumSize = minimumSize
    }

    val startStopReset = JButton("start")

    val firstRow = HorizontalBoxPanel(goal, problemPicker, startStopReset)

    val stepInto = JButton("step into (F12)")
    val stepOver = JButton("step over")
    val stepReturn = JButton("step return")

    fun setEnabledStepButtons(enabled: Boolean) {
        stepInto.isEnabled = enabled
        stepOver.isEnabled = enabled
        stepReturn.isEnabled = enabled
    }

    val secondRow = HorizontalBoxPanel(stepInto, stepOver, stepReturn).apply {
        setEmptyBorder(16)
    }

    val slider = JSlider(0, 11, 2)

    fun delayLogarithm(): Int {
        return if (slider.value == 0) -1 else slider.maximum - slider.value
    }

    init {
        setEnabledStepButtons(false)
        add(firstRow)
        add(secondRow)
        add(slider)
    }

    fun executionStarted() {
        problemPicker.isEnabled = false
        goal.isEnabled = false
        startStopReset.text = "stop"
        setEnabledStepButtons(true)
    }

    fun executionFinished() {
        problemPicker.isEnabled = true
        goal.isEnabled = true
        startStopReset.text = "reset"
        setEnabledStepButtons(false)
    }

    fun isRunning(): Boolean {
        return startStopReset.text === "stop"
    }
}
