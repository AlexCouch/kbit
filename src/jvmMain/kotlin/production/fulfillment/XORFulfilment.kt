package production.fulfillment

import BytecodeGeneratorCommand
import production.Expectation
import results.ErrorResult
import results.Result
import results.WrappedResult

@ExperimentalStdlibApi
class XORFulfillmentFactory(override val expectation: BytecodeGeneratorCommand.ExpectCommand.ExpectXORCommand) : ExpectationFulfillmentFactory<Expectation.XORExpectation, BytecodeGeneratorCommand.ExpectCommand.ExpectXORCommand>{
    override fun build(): Result<Expectation.XORExpectation> {
        val fulfilledCommand = expectation.fulfilledCommand?.toComponent() ?: return ErrorResult("XOR expectation ${expectation.name} not fulfilled. production.Expectation requires one of the following: \n\t${expectation.cases}")
        val component = when(fulfilledCommand){
            is WrappedResult -> fulfilledCommand.t
            is ErrorResult -> return ErrorResult("An error occurred while converting XOR expect fulfilled command to bytecode component", fulfilledCommand)
            else -> return ErrorResult("Unrecognized result: $fulfilledCommand")
        }
        return WrappedResult(Expectation.XORExpectation(expectation.name, expectation.description, component))
    }

}

/*
@ExperimentalStdlibApi
class XORFulfillmentFactory(private var expectCommand: BytecodeGeneratorCommand.ExpectCommand.ExpectXORCommand):
    BytecodeCommandFactory<BytecodeGeneratorCommand<*>> by ImplBytecodeCommandFactory(),
    ProvidesChunkProduction, ProvidesOpcodeProduction {

    override fun getChunk(name: String){
        val matchedPredicate = expectCommand.cases.find { it.name == name } ?: throw IllegalArgumentException("No chunk with name $name exists in expectation predicates for ${expectCommand.name}. Either change the name or create a chunk called $name")
        if(matchedPredicate !is BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand) throw IllegalStateException("No chunk found with name $name in predicates for expectation ${expectCommand.name}")
        if(expectCommand.fulfilledCommand != null) throw IllegalStateException("production.Expectation ${expectCommand.name} has already been fulfilled with ${expectCommand.fulfilledCommand}")
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
        if(expectCommand.fulfilledCommand != null) throw IllegalStateException("production.Expectation ${expectCommand.name} has already been fulfilled with ${expectCommand.fulfilledCommand}")
        this.expectCommand.fulfilledCommand = matchedPredicate
    }
}*/
