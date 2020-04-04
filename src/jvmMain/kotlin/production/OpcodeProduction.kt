package production

import BytecodeComponentBuilder
import Chunk
import Opcode
import recipe.ExpectationFulfillmentFactory
import recipe.OpcodeCommandFactory
import recipe.ProvidesFulfillment
import results.ErrorResult
import results.WrappedResult

@ExperimentalStdlibApi
interface ProvidesOpcodeProduction{
    fun getOpcode(name: String)
}

@ExperimentalStdlibApi
class GetOpcodeFactory(private var opcodeCommand: BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand): BytecodeComponentBuilder<Opcode>{
    override fun build(): Opcode =
        when(val result = opcodeCommand.toComponent()){
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