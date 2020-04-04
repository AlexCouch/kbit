package recipe

import BytecodeGeneratorEngine
import KBitGeneratorErrorManager
import production.BytecodeProductionEngine
import recipe.expectations.ExpectANDCommandFactory
import recipe.expectations.ExpectXORCommandFactory
import recipe.expectations.ProvidesExpectation

@ExperimentalStdlibApi
class BytecodeRecipeEngine : BytecodeGeneratorEngine<BytecodeProductionEngine>(),
    ProvidesChunkCreator, ProvidesOpcodeCreator, ProvidesExpectation {

    override val errorManager: KBitGeneratorErrorManager<*> = KBitGeneratorErrorManager(this)

    override fun createChunk(block: ChunkCommandFactory.()->Unit){
        val factory = DefaultChunkCommandFactory()
        factory.block()
        this.commands.add(factory.build())
    }

    override fun expectXOR(block: ExpectANDCommandFactory.()->Unit){
        val factory = ExpectANDCommandFactory()
        factory.block()
        this.commands.add(factory.build())
    }

    override fun expectAND(block: ExpectXORCommandFactory.() -> Unit) {
        val expectCaseCommandFactory = ExpectXORCommandFactory()
    }

    override fun createOpcode(block: OpcodeCommandFactory.() -> Unit) {
        val factory = OpcodeCommandFactory()
        factory.block()
        this.commands.add(factory.build())
    }

    override fun build(): BytecodeProductionEngine{
        this.commands.forEach{
            println(it.toString())
        }
        return BytecodeProductionEngine(this.commands)
    }
}