package parsing

data class Diagnostic(val position: Int, override val message: String) : Exception(message)
