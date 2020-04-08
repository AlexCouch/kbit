package production.fulfillment

import production.Expectation
import results.*

class ANDFulfillmentFactory(override val expectation: BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand): ExpectationFulfillmentFactory<Expectation.ANDExpectation, BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand>{
    override fun build(): Result<Expectation.ANDExpectation> {
        val choices = expectation.choices.map {
            when(val result = it.toComponent()){
                is WrappedResult -> result.t
                is ErrorResult -> return ErrorResult("An error occurred while converting AND expectation result to bytecode component", result)
                else -> return ErrorResult("Unrecognized result: $result")
            }
        }
        return WrappedResult(Expectation.ANDExpectation(expectation.name, expectation.description, choices))
    }

}