package recipe.expectations

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory
import recipe.ChunkCommandFactory
import recipe.DefaultChunkCommandFactory

@ExperimentalStdlibApi
class ExpectANDCommandFactory:
    BytecodeCommandFactory<BytecodeGeneratorCommand<*>> by ImplBytecodeCommandFactory(),
    ExpectationFactory by DefaultExpectationFactoryImpl() {

    override fun createChunk(block: ChunkCommandFactory.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun build(): BytecodeGeneratorCommand<*> =
        BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand(name, description, predicates)

}