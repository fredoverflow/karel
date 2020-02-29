package vm

data class IllegalBytecode(val bytecode: Int) : Exception("%04x".format(bytecode))
