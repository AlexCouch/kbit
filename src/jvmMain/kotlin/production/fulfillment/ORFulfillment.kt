package production.fulfillment

import results.ErrorResult
import results.WrappedResult

class ORFulfillment(override val expectation: BytecodeGeneratorCommand.ExpectCommand.ExpectORCommand): ExpectationFulfillmentFactory<Expectation.ANDExpectation, BytecodeGeneratorCommand.ExpectCommand.ExpectORCommand>{
    override fun build(): Expectation.ANDExpectation {
        return Expectation.ANDExpectation(expectation.name, expectation.description, expectation.fulfilledCommands.map {
            when(val result = it.toComponent()){
                is WrappedResult -> result.t
                is ErrorResult ->
            }
        })
    }

}