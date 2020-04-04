package production.fulfillment

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory
import production.GetChunkFactory
import production.ProvidesChunkProduction
import production.ProvidesOpcodeProduction

@ExperimentalStdlibApi
class XORFulfillmentFactory(private var expectCommand: BytecodeGeneratorCommand.ExpectCommand.ExpectXORCommand):
    BytecodeCommandFactory<BytecodeGeneratorCommand<*>> by ImplBytecodeCommandFactory(),
    ProvidesChunkProduction, ProvidesOpcodeProduction {

    override fun getChunk(name: String){
        val matchedPredicate = expectCommand.cases.find { it.name == name } ?: throw IllegalArgumentException("No chunk with name $name exists in expectation predicates for ${expectCommand.name}. Either change the name or create a chunk called $name")
        if(matchedPredicate !is BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand) throw IllegalStateException("No chunk found with name $name in predicates for expectation ${expectCommand.name}")
        if(expectCommand.fulfilledCommand != null) throw IllegalStateException("Expectation ${expectCommand.name} has already been fulfilled with ${expectCommand.fulfilledCommand}")
        this.expectCommand.fulfilledCommand = matchedPredicate
    }

    override fun getChunk(name: String, block: GetChunkFactory.() -> Unit) {
        val matchedPredicate = expectCommand.cases.find { it.name == name } ?: throw IllegalArgumentException("No chunk with name $name exists in expectation predicates for ${expectCommand.name}. Either change the name or create a chunk called $name")
        if(matchedPredicate !is BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand) throw IllegalStateException("No chunk found with name $name in predicates for expectation ${expectCommand.name}")
        val getChunkFactory = GetChunkFactory(matchedPredicate)
        getChunkFactory.block()
        this.expectCommand.fulfilledCommand = matchedPredicate
    }

    override fun getOpcode(name: String){
        val matchedPredicate = expectCommand.cases.find { it.name == name } ?: throw IllegalArgumentException("No opcode with name $name exists in expectation predicates for ${expectCommand.name}. Either change the name or create a chunk called $name")
        if(matchedPredicate !is BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand) throw IllegalStateException("No opcode found with name $name in predicates for expectation ${expectCommand.name}")
        if(expectCommand.fulfilledCommand != null) throw IllegalStateException("Expectation ${expectCommand.name} has already been fulfilled with ${expectCommand.fulfilledCommand}")
        this.expectCommand.fulfilledCommand = matchedPredicate
    }
}