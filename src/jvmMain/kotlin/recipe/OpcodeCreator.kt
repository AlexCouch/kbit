package recipe

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory

@ExperimentalStdlibApi
interface ProvidesOpcodeCreator{
    fun createOpcode(block: OpcodeCommandFactory.()->Unit)
}

@ExperimentalStdlibApi
class OpcodeCommandFactory: BytecodeCommandFactory<BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand> by ImplBytecodeCommandFactory() {
    private var code: Byte = -1

    infix fun code(code: Byte){
        this.code = code
    }

    override fun build(): BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand =
        BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand(
            this.name,
            this.description,
            this.code
        )
}