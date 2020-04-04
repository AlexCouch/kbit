package production

import Chunk
import production.fulfillment.ProvidesFulfillment
import recipe.expectations.ExpectXORCommandFactory
import results.ErrorResult
import results.WrappedResult

@ExperimentalStdlibApi
interface ProvidesChunkProduction{
    fun getChunk(name: String)
    fun getChunk(name: String, block: GetChunkFactory.()->Unit)
}

@ExperimentalStdlibApi
class GetChunkFactory(private var chunkCommand: BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand):
    ProvidesFulfillment {
    override fun fulfillXor(name: String, block: ExpectXORCommandFactory.()->Unit){
        val comp = this.chunkCommand.components.find { it.name == name } ?: throw IllegalArgumentException("There are no commands by the name $name that currently exist. Either change the name to something that exists or make one using this name.")
        if(comp !is BytecodeGeneratorCommand.ExpectCommand) throw IllegalStateException("The found command with name $name is not an expect command, but instead is $comp")
        val expectFulfillment = ExpectXORCommandFactory()
        expectFulfillment.block()
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
