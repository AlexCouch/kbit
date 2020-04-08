package recipe.expectations

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory
import recipe.ChunkCommandFactory
import recipe.DefaultChunkCommandFactory
import recipe.DefaultOpcodeCommandFactory
import recipe.OpcodeCommandFactory

@ExperimentalStdlibApi
class ExpectNANDCommandFactory:
    BytecodeCommandFactory<BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand> by ImplBytecodeCommandFactory(), ExpectationFactory by DefaultExpectationFactoryImpl(){
    override fun createChunk(block: ChunkCommandFactory.() -> Unit) {
        val factory = DefaultChunkCommandFactory()
        factory.block()
        val command = factory.build()
        predicates.add(command)
    }

    override fun createOpcode(block: OpcodeCommandFactory.() -> Unit) {
        val factory = DefaultOpcodeCommandFactory()
        factory.block()
        val command = factory.build()
        predicates.add(command)
    }

    override fun build() = BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand(name, description, predicates)

}