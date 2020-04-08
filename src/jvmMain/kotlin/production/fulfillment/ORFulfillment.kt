package production.fulfillment

import production.Expectation
import results.*

class ORFulfillmentFactory(override val expectation: BytecodeGeneratorCommand.ExpectCommand.ExpectORCommand): ExpectationFulfillmentFactory<Expectation.ORExpectation, BytecodeGeneratorCommand.ExpectCommand.ExpectORCommand>{
    override fun build(): Result<Expectation.ORExpectation> {
        return WrappedResult(Expectation.ORExpectation(expectation.name, expectation.description, expectation.fulfilledCommands.map {
            when(val result = it.toComponent()){
                is WrappedResult -> result.t
                is ErrorResult -> return ErrorResult("An error occurred while converting OR expectation fulfillment command to bytecode component", result)
                else -> return ErrorResult("Unrecognized result: $result")
            }
        }))
    }

}