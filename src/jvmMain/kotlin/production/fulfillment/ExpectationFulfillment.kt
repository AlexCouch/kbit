package production.fulfillment

import production.Expectation
import production.ProvidesChunkProduction
import production.ProvidesOpcodeProduction
import results.*

@ExperimentalStdlibApi
interface ProvidesFulfillment{
    suspend fun fulfillXor(name: String, block: suspend XORFulfillmentFactory.()->Unit)
    suspend fun fulfillAnd(name: String, block: suspend ANDFulfillmentFactory.()->Unit)
    suspend fun fulfillOr(name: String, block: suspend ORFulfillmentFactory.()->Unit)
    suspend fun fulfillNor(name: String, block: suspend NORFulfillmentFactory.()->Unit)
    suspend fun fulfillNand(name: String, block: suspend NANDFulfillmentFactory.()->Unit)
}

@ExperimentalStdlibApi
interface ExpectationFulfillmentFactory<T: Expectation, R: BytecodeGeneratorCommand.ExpectCommand<*>>: ProvidesChunkProduction, ProvidesOpcodeProduction{
    val expectation: R
    fun build(): Result<T>
}