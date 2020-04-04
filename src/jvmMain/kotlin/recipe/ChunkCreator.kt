package recipe

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory
import recipe.expectations.ExpectANDCommandFactory
import recipe.expectations.ExpectXORCommandFactory
import recipe.expectations.ProvidesExpectation

@ExperimentalStdlibApi
interface ProvidesChunkCreator: BytecodeCommandFactory<BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand>{
    fun createChunk(block: ChunkCommandFactory.()->Unit)
}

@ExperimentalStdlibApi
interface ProvidesPseudoChunkCreator{
    fun createChunk(block: ChunkCommandFactory.()->Unit)
}

@ExperimentalStdlibApi
interface ChunkCommandFactory: ProvidesOpcodeCreator, ProvidesChunkCreator,
    ProvidesExpectation

@ExperimentalStdlibApi
class DefaultChunkCommandFactory:
    BytecodeCommandFactory<BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand> by ImplBytecodeCommandFactory(),
    ChunkCommandFactory{
    private val children = ArrayDeque<BytecodeGeneratorCommand<*>>()

    override fun createOpcode(block: OpcodeCommandFactory.()->Unit){
        val factory = OpcodeCommandFactory()
        factory.block()
        this.children.add(factory.build())
    }

    override fun createChunk(block: ChunkCommandFactory.() -> Unit) {
        val factory = DefaultChunkCommandFactory()
        factory.block()
        this.children.add(factory.build())
    }

    override fun expectXOR(block: ExpectANDCommandFactory.()->Unit){
        val factory = ExpectANDCommandFactory()
        factory.block()
        this.children.add(factory.build())
    }

    override fun expectAND(block: ExpectXORCommandFactory.() -> Unit) {
        val factory = ExpectXORCommandFactory()
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