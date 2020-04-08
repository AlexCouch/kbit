package recipe.expectations

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory

@ExperimentalStdlibApi
class ExpectANDCommandFactory : BytecodeCommandFactory<BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand> by ImplBytecodeCommandFactory(), ExpectationFactory by DefaultExpectationFactoryImpl(){
    override fun build(): BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand = BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand(this.name, this.description, predicates)
}

/*
@ExperimentalStdlibApi
class ExpectANDCommandFactory:
    BytecodeCommandFactory<BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand> by ImplBytecodeCommandFactory(),
    ExpectationFactory {

    override fun build() = BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand(name, description, predicates)

}*/
