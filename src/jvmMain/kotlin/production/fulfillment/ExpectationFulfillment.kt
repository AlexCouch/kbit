package production.fulfillment

import Expectation
import recipe.expectations.*

@ExperimentalStdlibApi
interface ProvidesFulfillment{
    suspend fun fulfillXor(name: String, block: XORFulfillmentFactory.()->Unit)
    suspend fun fulfillAnd(name: String, block: ANDFulfillmentFactory.()->Unit)
    suspend fun fulfillOr(name: String, block: ORFulfillmentFactory.()->Unit)
    suspend fun fulfillNor(name: String, block: ExpectNORCommandFactory.()->Unit)
    suspend fun fulfillNand(name: String, block: ExpectNANDCommandFactory.()->Unit)
}

interface ExpectationFulfillmentFactory<T: Expectation, R: BytecodeGeneratorCommand.ExpectCommand<*>>{
    val expectation: R
    fun build(): T
}

class ExpectationFulfillment: KBitErrorManager