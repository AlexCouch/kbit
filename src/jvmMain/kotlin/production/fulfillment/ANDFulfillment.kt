package production.fulfillment

import results.ErrorResult
import results.WrappedResult

class ANDFulfillmentFactory(override val expectation: BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand): ExpectationFulfillmentFactory<Expectation.ANDExpectation, BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand>{
    override fun build(): Expectation.ANDExpectation {
        val choices = expectation.choices.map {
            if(!it.value) ErrorHandler.reportError {
                this.appendWithNewLine("AND Expectation requires that all cases must be fulfilled, except one is not fulfilled: ${it.key}")
            }
            when(val result = it.key.toComponent()){
                is WrappedResult -> result.t
                is ErrorResult -> ErrorHandler.reportError {
                    this.appendWithNewLine("An error occurred while converting AND Expectation case to bytecode component")
                    this.appendWithNewLine(result.toString())
                }
                else -> ErrorHandler.reportError{

                }
            }
        }
    }

}