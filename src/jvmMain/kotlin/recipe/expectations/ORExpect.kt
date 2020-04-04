package recipe.expectations

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory
import recipe.ChunkCommandFactory

@ExperimentalStdlibApi
class ExpectORCommandFactory:
    BytecodeCommandFactory<BytecodeGeneratorCommand> by ImplBytecodeCommandFactory(),
    ExpectationFactory by DefaultExpectationFactoryImpl() {

    override fun build(): BytecodeGeneratorCommand =
        BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand(name, description, predicates)

}