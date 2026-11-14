package vm

class IllegalOperation(operation: Byte) : Exception("%02x".format(operation))
