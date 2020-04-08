package production.fulfillment

import production.Expectation
import results.ErrorResult
import results.Result
import results.WrappedResult

class NORFulfillmentFactory(override val expectation: BytecodeGeneratorCommand.ExpectCommand.ExpectNORCommand): ExpectationFulfillmentFactory<Expectation.NORExpectation, BytecodeGeneratorCommand.ExpectCommand.ExpectNORCommand>{
    override fun build(): Result<Expectation.NORExpectation> {
        return WrappedResult(Expectation.NORExpectation(expectation.name, expectation.description, expectation.fulfilledCommands.map {
            when(val result = it.toComponent()){
                is WrappedResult -> result.t
                is ErrorResult -> return ErrorResult("An error occurred while converting NOR expectation fulfillment command to bytecode component", result)
                else -> return ErrorResult("Unrecognized result: $result")
            }
        }))
    }

}