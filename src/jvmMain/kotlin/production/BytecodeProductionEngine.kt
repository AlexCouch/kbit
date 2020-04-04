package production

import BytecodeComponent
import BytecodeGeneratorCommand
import BytecodeGeneratorEngine
import Chunk
import KBitGeneratorErrorManager
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import production.fulfillment.ProvidesFulfillment
import recipe.expectations.*
import results.ErrorResult
import results.WrappedResult

@ExperimentalStdlibApi
class BytecodeProductionEngine(override val commands: ArrayDeque<BytecodeGeneratorCommand<*>>):
    BytecodeGeneratorEngine<ByteReadPacket>(), ProvidesChunkProduction,
    ProvidesFulfillment {
    override val errorManager: KBitGeneratorErrorManager = KBitGeneratorErrorManager(this)
    private val bytes = ArrayDeque<BytecodeComponent>()
    private fun findChunk(name: String): BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand{
        val found = this.commands.find { it.name == name } ?: throw IllegalArgumentException("Attempted to get chunk with name $name but it doesn't exist")
        if(found !is BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand) throw IllegalArgumentException("Attempted to get chunk with $name but it is not a chunk but is instead $found")
        return found
    }
    override fun getChunk(name: String){
        val comp = findChunk(name)
        val expectations = comp.components.filterIsInstance<BytecodeGeneratorCommand.ExpectCommand<*>>()
        if(expectations.isNotEmpty()){
            for(expectation in expectations){
                if(!expectation.fulfilled){
                    return ErrorHandler.reportError {
                        this.appendWithNewLine("Chunk $name has expectations to be fulfilled:")
                        this.indent {
                            this.append(expectation.toString())
                        }
                    }
                }
            }
        }
        val chunk = when(val result = comp.toComponent()){
            is WrappedResult -> result.t
            is ErrorResult -> {
                val error = ErrorResult<Chunk>("An error occurred while converting create chunk command to chunk component", result)
                println(error)
                throw IllegalStateException(error.toString())
            }
            else -> {
                val error = ErrorResult<Chunk>("Unrecognized result: $result")
                println(error)
                throw IllegalStateException(error.toString())
            }
        }
        bytes.add(chunk)
    }

    override fun getChunk(name: String, block: GetChunkFactory.()->Unit){
        val comp = this.findChunk(name)
        val getChunkFactory = GetChunkFactory(comp)
        getChunkFactory.block()
        val chunk = getChunkFactory.build()
        this.bytes.add(chunk)
    }

    override fun build(): ByteReadPacket =buildPacket {
        bytes.forEach {
            this.writePacket(it.toBytePacket())
        }
    }

    fun buildBytePacket(block: BytecodeProductionEngine.() -> Unit): ByteReadPacket {
        this.block()
        return this.build()
    }

    override suspend fun fulfillXor(name: String, block: ExpectXORCommandFactory.() -> Unit) {
        val factory = ExpectXORCommandFactory()
        factory.block()
        when(val result = factory.build().toComponent()){
            is WrappedResult -> this.bytes.add(result.t)
            is ErrorResult -> this.errorManager.createError("fulfillXor", "An error occurred while building a component out of an XOR fulfillment: $result")
        }

    }

    override suspend fun fulfillAnd(name: String, block: ExpectANDCommandFactory.() -> Unit) {
        val factory = ExpectANDCommandFactory()
        factory.block()
        when(val result = factory.build().toComponent()){
            is WrappedResult -> this.bytes.add(result.t)
            is ErrorResult -> this.errorManager.createError("fulfillAnd", "An error occurred while building a component out of an XOR fulfillment: $result")
        }
    }

    override suspend fun fulfillOr(name: String, block: ExpectORCommandFactory.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun fulfillNor(name: String, block: ExpectNORCommandFactory.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun fulfillNand(name: String, block: ExpectNANDCommandFactory.() -> Unit) {
        TODO("Not yet implemented")
    }
}