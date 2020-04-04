package recipe.expectations

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory

@ExperimentalStdlibApi
class ExpectNORCommandFactory:
    BytecodeCommandFactory<BytecodeGeneratorCommand> by ImplBytecodeCommandFactory(),
    ExpectationFactory by DefaultExpectationFactoryImpl() {

    override fun build(): BytecodeGeneratorCommand =
        BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand(name, description, predicates)

}