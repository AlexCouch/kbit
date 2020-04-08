package recipe.expectations

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory

@ExperimentalStdlibApi
class ExpectXORCommandFactory:
    BytecodeCommandFactory<BytecodeGeneratorCommand.ExpectCommand.ExpectXORCommand> by ImplBytecodeCommandFactory(),
    ExpectationFactory by DefaultExpectationFactoryImpl() {
    override fun build()  = BytecodeGeneratorCommand.ExpectCommand.ExpectXORCommand(this.name, this.description, this.predicates)
}