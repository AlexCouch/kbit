package recipe

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory

@ExperimentalStdlibApi
interface ProvidesOpcodeCreator{
    fun createOpcode(block: OpcodeCommandFactory.()->Unit)
}

@ExperimentalStdlibApi
abstract class OpcodeCommandFactory: BytecodeCommandFactory<BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand> by ImplBytecodeCommandFactory(){
    var code: Byte = -1
}

@ExperimentalStdlibApi
class DefaultOpcodeCommandFactory: OpcodeCommandFactory() {
    override fun build(): BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand =
        BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand(
            this.name,
            this.description,
            this.code
        )
}