package vm

class IllegalBytecode(bytecode: Int) : Exception("%04x".format(bytecode))
