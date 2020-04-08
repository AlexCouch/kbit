package production.fulfillment

import production.Expectation
import results.ErrorResult
import results.Result
import results.WrappedResult

class NANDFulfillmentFactory(override val expectation: BytecodeGeneratorCommand.ExpectCommand.ExpectNANDCommand) : ExpectationFulfillmentFactory<Expectation.NANDExpectation, BytecodeGeneratorCommand.ExpectCommand.ExpectNANDCommand>{
    override fun build(): Result<Expectation.NANDExpectation> {
        val commands = expectation.fulfilledCommands.map {
            when(val result = it.toComponent()){
                is WrappedResult -> result.t
                is ErrorResult -> return ErrorResult("An error occurred while converting NAND expectation to bytecode component")
                else -> return ErrorResult("Unrecognized result: $result")
            }
        }
        return WrappedResult(Expectation.NANDExpectation(expectation.name, expectation.description, commands))
    }

}