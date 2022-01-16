package vm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import syntax.lexer.Token
import syntax.tree.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

private const val CONSTRUCTOR_DESCRIPTOR = "(Ljava/util/concurrent/atomic/AtomicReference;Lvm/Karel\$Callbacks;)V"
private const val INSTRUMENT = true
private val id = AtomicInteger(0)

private val methodDescriptors = HashMap<Int, String>()

fun methodDescriptor(countMethodHandles: Int): String {
    return methodDescriptors.computeIfAbsent(countMethodHandles) { n ->
        "(I" + "Ljava/lang/invoke/MethodHandle;".repeat(n) + ")V"
    }
}

class Emitter(private val program: Program) {
    private val className = "Karel_${id.incrementAndGet()}"
    private val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    private lateinit var mv: MethodVisitor

    fun emit(): Class<out Karel> {
        classWriter.visit(V1_8, ACC_PUBLIC, className, null, "vm/Karel", null)

        emitConstructor()

        for (command in program.commands) {
            command.emit()
        }

        classWriter.visitEnd()
        val classLoader = ByteArrayClassLoader()
        val byteArray = classWriter.toByteArray()
        if (id.get() == 0) {
            Files.write(Path.of(System.getProperty("user.home"), "Karel_0.class"), byteArray)
        }
        @Suppress("UNCHECKED_CAST")
        return classLoader.defineClass(className, byteArray) as Class<out Karel>
    }

    private fun emitConstructor() {
        mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", CONSTRUCTOR_DESCRIPTOR, null, null)
        mv.visitCode()

        // super(atomicWorld, callbacks)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitVarInsn(ALOAD, 2)
        mv.visitMethodInsn(INVOKESPECIAL, "vm/Karel", "<init>", CONSTRUCTOR_DESCRIPTOR, false)

        mv.visitInsn(RETURN)
        mv.visitEnd()
        mv.visitMaxs(0, 0) // compute automatically
    }

    private var currentCommand = program.commands.first()

    private fun Command.emit() {
        currentCommand = this
        mv = classWriter.visitMethod(ACC_PUBLIC, identifier.lexeme, methodDescriptor(parameters.size), null, null)
        mv.visitCode()

        emitPrologue()
        body.emit()
        emitEpilogue()

        mv.visitInsn(RETURN)
        mv.visitEnd()
        mv.visitMaxs(0, 0) // compute automatically
    }

    private fun Command.emitPrologue() {
        if (INSTRUMENT) {
            // callbacks.pauseAt(callerPosition)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, "vm/Karel", "callbacks", "Lvm/Karel\$Callbacks;")
            mv.visitVarInsn(ILOAD, 1)
            mv.visitMethodInsn(INVOKEINTERFACE, "vm/Karel\$Callbacks", "pauseAt", "(I)V", true)

            // callbacks.enter(callerPosition, identifier.start)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, "vm/Karel", "callbacks", "Lvm/Karel\$Callbacks;")
            mv.visitVarInsn(ILOAD, 1)
            mv.visitPushInt(identifier.start)
            mv.visitMethodInsn(INVOKEINTERFACE, "vm/Karel\$Callbacks", "enter", "(II)V", true)
        }
    }

    private fun Command.emitEpilogue() {
        if (INSTRUMENT) {
            pauseAt(body.closingBrace)

            // callbacks.leave()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, "vm/Karel", "callbacks", "Lvm/Karel\$Callbacks;")
            mv.visitMethodInsn(INVOKEINTERFACE, "vm/Karel\$Callbacks", "leave", "()V", true)
        }
    }

    private fun pauseAt(token: Token) {
        if (INSTRUMENT) {
            // callbacks.pauseAt(token.start)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, "vm/Karel", "callbacks", "Lvm/Karel\$Callbacks;")
            mv.visitPushInt(token.start)
            mv.visitMethodInsn(INVOKEINTERFACE, "vm/Karel\$Callbacks", "pauseAt", "(I)V", true)
        }
    }

    private fun Condition.optimizeForwardJump() = when (this) {
        is Not -> Pair(p, IFNE)
        else -> Pair(this, IFEQ)
    }

    private fun Statement.emit() {
        when (this) {
            is Block -> {
                for (statement in statements) {
                    statement.emit()
                }
            }
            is IfThenElse -> {
                val (condition, jump) = condition.optimizeForwardJump()
                pauseAt(iF)
                if (e1se == null) {
                    condition.emit()
                    val over = Label()
                    mv.visitJumpInsn(jump, over)
                    th3n.emit()
                    mv.visitLabel(over)
                } else {
                    condition.emit()
                    val overThen = Label()
                    mv.visitJumpInsn(jump, overThen)
                    th3n.emit()
                    val overElse = Label()
                    mv.visitJumpInsn(GOTO, overElse)
                    mv.visitLabel(overThen)
                    e1se.emit()
                    mv.visitLabel(overElse)
                }
            }
            is While -> {
                val (condition, jump) = condition.optimizeForwardJump()
                val back = Label()
                mv.visitLabel(back)
                pauseAt(whi1e)
                condition.emit()
                val over = Label()
                mv.visitJumpInsn(jump, over)
                body.emit()
                mv.visitJumpInsn(GOTO, back)
                mv.visitLabel(over)
            }
            is Repeat -> {
                pauseAt(repeat)
                mv.visitPushInt(this.times)
                val back = Label()
                mv.visitLabel(back)
                body.emit()
                pauseAt(body.closingBrace)
                mv.visitInsn(ICONST_1)
                mv.visitInsn(ISUB)
                mv.visitInsn(DUP)
                mv.visitJumpInsn(IFNE, back)
                mv.visitInsn(POP)
            }
            is Call -> {
                val parameterIndex = currentCommand.parameters.indexOfFirst { it.lexeme == target.lexeme }
                if (parameterIndex != -1) {
                    // parameter();
                    mv.visitVarInsn(ALOAD, 2 + parameterIndex)
                    pushThisAndPosition()
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        "java/lang/invoke/MethodHandle",
                        "invokeExact",
                        "(L$className;I)V",
                        false
                    )
                } else {
                    // command(arguments);
                    pushThisAndPosition()
                    for (argument in arguments) {
                        val parameterIndex = currentCommand.parameters.indexOfFirst { it.lexeme == argument.lexeme }
                        if (parameterIndex != -1) {
                            // argument is parameter
                            mv.visitVarInsn(ALOAD, 2 + parameterIndex)
                        } else {
                            // argument is command
                            mv.visitLdcInsn(Handle(H_INVOKEVIRTUAL, className, argument.lexeme, "(I)V", false))
                        }
                    }
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, target.lexeme, methodDescriptor(arguments.size), false)
                }
            }
        }
    }

    private fun Call.pushThisAndPosition() {
        mv.visitVarInsn(ALOAD, 0)
        if (INSTRUMENT) {
            mv.visitPushInt(target.start)
        } else {
            mv.visitInsn(ICONST_0)
        }
    }

    private fun Condition.emit() {
        when (this) {
            is False -> {
                mv.visitInsn(ICONST_0)
            }
            is True -> {
                mv.visitInsn(ICONST_1)
            }
            is Predicate -> {
                mv.visitVarInsn(ALOAD, 0)
                mv.visitMethodInsn(INVOKEVIRTUAL, className, predicate.lexeme, "()Z", false)
            }
            is Not -> {
                p.emit()
                mv.visitInsn(ICONST_1)
                mv.visitInsn(IXOR)
            }
            is Conjunction -> {
                p.emit()
                q.emit()
                mv.visitInsn(IAND)
            }
            is Disjunction -> {
                p.emit()
                q.emit()
                mv.visitInsn(IOR)
            }
        }
    }
}

fun MethodVisitor.visitPushInt(x: Int) {
    when (x) {
        in -1..5 -> {
            visitInsn(ICONST_0 + x)
        }
        in -128..127 -> {
            visitIntInsn(BIPUSH, x)
        }
        in -32768..32767 -> {
            visitIntInsn(SIPUSH, x)
        }
        else -> {
            visitLdcInsn(x)
        }
    }
}
