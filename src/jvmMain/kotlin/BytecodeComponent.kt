import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket

interface BytecodeComponentBuilder<T : BytecodeComponent>{
    fun build(): T
}

interface BytecodeComponent{
    val name: String
    val description: String
    fun toBytePacket(): ByteReadPacket
}

data class Opcode(override val name: String, override val description: String, val code: Byte): BytecodeComponent {
    override fun toBytePacket(): ByteReadPacket = buildPacket {
        this.writeByte(code)
    }
}

@ExperimentalStdlibApi
data class Chunk(override val name: String, override val description: String, val opcodes: ArrayDeque<BytecodeComponent>): BytecodeComponent {
    override fun toBytePacket(): ByteReadPacket = buildPacket{
        opcodes.forEach {
            this.writePacket(it.toBytePacket())
        }
    }
}

sealed class Expectation(override val name: String, override val description: String): BytecodeComponent{
    data class XORExpectation(
        override val name: String,
        override val description: String,
        val component: BytecodeComponent
    ): Expectation(name, description) {
        override fun toBytePacket(): ByteReadPacket = component.toBytePacket()
    }

    data class ANDExpectation(
        override val name: String,
        override val description: String,
        val components: List<BytecodeComponent>
    ): Expectation(name, description) {
        override fun toBytePacket(): ByteReadPacket = buildPacket{
            components.forEach {
                this.writePacket(it.toBytePacket())
            }
        }
    }
    data class ORExpectation(
        override val name: String,
        override val description: String,
        val components: List<BytecodeComponent>
    ): Expectation(name, description) {
        override fun toBytePacket(): ByteReadPacket = buildPacket{
            components.forEach {
                this.writePacket(it.toBytePacket())
            }
        }
    }
    data class NANDExpectation(
        override val name: String,
        override val description: String,
        val components: List<BytecodeComponent>
    ): Expectation(name, description) {
        override fun toBytePacket(): ByteReadPacket = buildPacket{
            components.forEach {
                this.writePacket(it.toBytePacket())
            }
        }
    }
    data class NORExpectation(
        override val name: String,
        override val description: String,
        val components: List<BytecodeComponent>
    ): Expectation(name, description) {
        override fun toBytePacket(): ByteReadPacket = buildPacket{
            components.forEach {
                this.writePacket(it.toBytePacket())
            }
        }
    }
}