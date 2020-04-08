package recipe

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory
import recipe.expectations.DefaultExpectationFactoryImpl
import recipe.expectations.ExpectANDCommandFactory
import recipe.expectations.ExpectXORCommandFactory
import recipe.expectations.ProvidesExpectation

@ExperimentalStdlibApi
interface ProvidesChunkCreator{
    fun createChunk(block: ChunkCommandFactory.()->Unit)
}

@ExperimentalStdlibApi
abstract class ChunkCommandFactory: BytecodeCommandFactory<BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand> by ImplBytecodeCommandFactory(),
    ProvidesOpcodeCreator, ProvidesChunkCreator,
    ProvidesExpectation by DefaultExpectationFactoryImpl()

@ExperimentalStdlibApi
class DefaultChunkCommandFactory:
    ChunkCommandFactory(){
    private val children = ArrayDeque<BytecodeGeneratorCommand<*>>()

    override fun createOpcode(block: OpcodeCommandFactory.()->Unit){
        val factory = DefaultOpcodeCommandFactory()
        factory.block()
        this.children.add(factory.build())
    }

    override fun createChunk(block: ChunkCommandFactory.() -> Unit) {
        val factory = DefaultChunkCommandFactory()
        factory.block()
        this.children.add(factory.build())
    }

    override fun expectXOR(block: ExpectXORCommandFactory.()->Unit){
        val factory = ExpectXORCommandFactory()
        factory.block()
        this.children.add(factory.build())
    }

    override fun expectAND(block: ExpectANDCommandFactory.() -> Unit) {
        val factory = ExpectANDCommandFactory()
        factory.block()
        this.children.add(factory.build())
    }

    override fun build(): BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand =
        BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand(
            this.name,
            this.description,
            this.children
        )
}