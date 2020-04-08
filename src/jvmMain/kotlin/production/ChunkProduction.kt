package production

import kotlinx.io.core.BytePacketBuilder
import production.fulfillment.*
import recipe.ChunkCommandFactory
import results.ErrorResult
import results.WrappedResult

@ExperimentalStdlibApi
interface ProvidesChunkProduction{
    suspend fun findChunk(name: String): BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand
    suspend fun getChunk(name: String)
    suspend fun getChunk(name: String, block: suspend DefaultGetChunkFactory.()->Unit)
}

@ExperimentalStdlibApi
interface ChunkProductionFactory: ProvidesFulfillment, ProvidesChunkProduction, ProvidesOpcodeProduction

@ExperimentalStdlibApi
class DefaultGetChunkFactory(override val command: BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand): BytecodeProductionFactory<Chunk, BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand>, ChunkProductionFactory {

    override val bytes: BytePacketBuilder = BytePacketBuilder()

    override suspend fun fulfillXor(name: String, block: suspend XORFulfillmentFactory.()->Unit){
        val comp = command.components.filterIsInstance<BytecodeGeneratorCommand.ExpectCommand.ExpectXORCommand>().find { it.name == name } ?: throw IllegalArgumentException("There are no commands by the name $name that currently exist. Either change the name to something that exists or make one using this name.")
        val expectFulfillment = XORFulfillmentFactory(comp)
        expectFulfillment.block()
    }

    override suspend fun fulfillAnd(name: String, block: suspend ANDFulfillmentFactory.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun fulfillOr(name: String, block: suspend ORFulfillmentFactory.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun fulfillNor(name: String, block: suspend NORFulfillmentFactory.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun fulfillNand(name: String, block: suspend NANDFulfillmentFactory.() -> Unit) {
        TODO("Not yet implemented")
    }

    private fun findChunk(name: String): BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand{
        val found = command.components.find { it.name == name } ?: throw IllegalArgumentException("Attempted to get chunk with name $name but it doesn't exist")
        if(found !is BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand) throw IllegalArgumentException("Attempted to get chunk with $name but it is not a chunk but is instead $found")
        return found
    }

    override suspend fun getChunk(name: String){
        val comp = findChunk(name)
        val expectations = comp.components.filterIsInstance<BytecodeGeneratorCommand.ExpectCommand<*>>()
        if(expectations.isNotEmpty()){
            for(expectation in expectations){
                if(!expectation.fulfilled){
                    return ErrorHandler.reportError {
                        this.appendWithNewLine("production.Chunk $name has expectations to be fulfilled:")
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

    override suspend fun getChunk(name: String, block: suspend DefaultGetChunkFactory.()->Unit){
        val comp = findChunk(name)
        val getChunkFactory = DefaultGetChunkFactory(comp)
        getChunkFactory.block()
        val chunk = getChunkFactory.build()
        bytes.add(chunk)
    }

    override fun getOpcode(name: String) {
        TODO("Not yet implemented")
    }

    fun build(): Chunk =
        when(val result = chunkCommand.toComponent()){
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
}
