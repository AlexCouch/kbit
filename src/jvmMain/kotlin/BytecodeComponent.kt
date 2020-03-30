import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket

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