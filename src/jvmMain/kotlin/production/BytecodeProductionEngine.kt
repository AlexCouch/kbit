package production

import BytecodeGeneratorCommand
import FinalBytecodeGeneratorEngine
import KBitGeneratorErrorManager
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import production.fulfillment.*
import results.ErrorResult
import results.WrappedResult

interface BytecodeProductionFactory<T: BytecodeComponent, out R: BytecodeGeneratorCommand<T>>{
    val command: R
    val bytes: BytePacketBuilder
    fun build(): T
}

@ExperimentalStdlibApi
class BytecodeProductionEngine(override val commands: List<BytecodeGeneratorCommand<*>>):
    FinalBytecodeGeneratorEngine<ByteReadPacket>(commands), ProvidesChunkProduction by DefaultGetChunkFactory(),
    ProvidesFulfillment {
    override val errorManager: KBitGeneratorErrorManager<*> = KBitGeneratorErrorManager(this)
    private val bytes = ArrayDeque<BytecodeComponent>()


    override suspend fun fulfillXor(name: String, block: suspend XORFulfillmentFactory.() -> Unit) {
        val expectCommand = commands.filterIsInstance<BytecodeGeneratorCommand.ExpectCommand.ExpectXORCommand>()
            .find { it.name == name }
        if(expectCommand == null){
            errorManager.createError("fulfillXor", "No xor expectation with name $name exists in top level recipe")
            return
        }
        val factory = XORFulfillmentFactory(expectCommand)
        factory.block()
        when(val result = factory.build()){
            is WrappedResult -> bytes.add(result.t)
            is ErrorResult -> {
                errorManager.createError("fulfillXor", "An error occurred while building a component out of an XOR fulfillment: $result")
                return
            }
            else -> {
                errorManager.createError("fulfillXor", "Unrecognized result: $result")
                return
            }
        }
    }

    override suspend fun fulfillAnd(name: String, block: suspend ANDFulfillmentFactory.() -> Unit) {
        val expectCommand = commands.filterIsInstance<BytecodeGeneratorCommand.ExpectCommand.ExpectANDCommand>()
            .find { it.name == name }
        if(expectCommand == null){
            errorManager.createError("fulfillAnd", "No AND expectation with name $name exists in top level recipe")
            return
        }
        val factory = ANDFulfillmentFactory(expectCommand)
        factory.block()
        when(val result = factory.build()){
            is WrappedResult -> bytes.add(result.t)
            is ErrorResult -> {
                errorManager.createError("fulfillAnd", "An error occurred while building a component out of an AND fulfillment: $result")
                return
            }
            else -> {
                errorManager.createError("fulfillAnd", "Unrecognized result: $result")
                return
            }
        }
    }

    override suspend fun fulfillOr(name: String, block: suspend ORFulfillmentFactory.() -> Unit) {
        val expectCommand = commands.filterIsInstance<BytecodeGeneratorCommand.ExpectCommand.ExpectORCommand>()
            .find { it.name == name }
        if(expectCommand == null){
            errorManager.createError("fulfillOr", "No OR expectation with name $name exists in top level recipe")
            return
        }
        val factory = ORFulfillmentFactory(expectCommand)
        factory.block()
        when(val result = factory.build()){
            is WrappedResult -> bytes.add(result.t)
            is ErrorResult -> {
                errorManager.createError("fulfillOr", "An error occurred while building a component out of an OR fulfillment: $result")
                return
            }
            else -> {
                errorManager.createError("fulfillOr", "Unrecognized result: $result")
                return
            }
        }
    }

    override suspend fun fulfillNor(name: String, block: suspend NORFulfillmentFactory.() -> Unit) {
        val expectCommand = commands.filterIsInstance<BytecodeGeneratorCommand.ExpectCommand.ExpectNORCommand>()
            .find { it.name == name }
        if(expectCommand == null){
            errorManager.createError("fulfillNor", "No NOR expectation with name $name exists in top level recipe")
            return
        }
        val factory = NORFulfillmentFactory(expectCommand)
        factory.block()
        when(val result = factory.build()){
            is WrappedResult -> bytes.add(result.t)
            is ErrorResult -> {
                errorManager.createError("fulfillNor", "An error occurred while building a component out of an NOR fulfillment: $result")
                return
            }
            else -> {
                errorManager.createError("fulfillNor", "Unrecognized result: $result")
                return
            }
        }
    }

    override suspend fun fulfillNand(name: String, block: suspend NANDFulfillmentFactory.() -> Unit) {
            val expectCommand = commands.filterIsInstance<BytecodeGeneratorCommand.ExpectCommand.ExpectNANDCommand>()
            .find { it.name == name }
        if(expectCommand == null){
            errorManager.createError("fulfillNand", "No NAND expectation with name $name exists in top level recipe")
            return
        }
        val factory = NANDFulfillmentFactory(expectCommand)
        factory.block()
        when(val result = factory.build()){
            is WrappedResult -> bytes.add(result.t)
            is ErrorResult -> {
                errorManager.createError("fulfillNand", "An error occurred while building a component out of an NAND fulfillment: $result")
                return
            }
            else -> {
                errorManager.createError("fulfillNand", "Unrecognized result: $result")
                return
            }
        }
    }

    override fun finalize(): ByteReadPacket = buildPacket{
        bytes.forEach {
            this.writePacket(it.toBytePacket())
        }
    }
}