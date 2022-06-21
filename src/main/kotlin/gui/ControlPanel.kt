package gui

import logic.Problem

import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JSlider

class ControlPanel(problems: List<Problem>) : VerticalBoxPanel() {

    val randomize = JButton("\uD83C\uDFB2").apply {
        isEnabled = false
    }

    val goal = JButton("goal")

    val problemPicker = JComboBox(problems.toTypedArray()).apply {
        maximumSize = minimumSize
    }

    val startStopReset = JButton("start")
    val check = JButton("\uD83D\uDC1C")

    val firstRow = HorizontalBoxPanel(randomize, goal, problemPicker, startStopReset, check)

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
        randomize.isEnabled = false
        goal.isEnabled = false
        problemPicker.isEnabled = false
        startStopReset.text = "stop"
        check.isEnabled = false
        setEnabledStepButtons(true)
    }

    fun executionFinished(isRandom: Boolean) {
        randomize.isEnabled = isRandom
        goal.isEnabled = true
        problemPicker.isEnabled = true
        startStopReset.text = "reset"
        check.isEnabled = true
        setEnabledStepButtons(false)
    }

    fun checkStarted() {
        randomize.isEnabled = false
        goal.isEnabled = false
        problemPicker.isEnabled = false
        startStopReset.isEnabled = false
        check.isEnabled = false
    }

    fun checkFinished(isRandom: Boolean) {
        randomize.isEnabled = isRandom
        goal.isEnabled = true
        problemPicker.isEnabled = true
        startStopReset.isEnabled = true
        check.isEnabled = true
    }

    fun isRunning(): Boolean {
        return startStopReset.text === "stop"
    }
}
