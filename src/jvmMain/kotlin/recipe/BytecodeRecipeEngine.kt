package recipe

import BytecodeGeneratorCommand
import KBitGeneratorErrorManager
import MutableBytecodeGeneratorEngine
import ProvidesPipeline
import consumption.BytecodeConsumptionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import production.BytecodeProductionEngine
import recipe.expectations.*

@ExperimentalStdlibApi
class BytecodeRecipeProvider(private val recipe: List<BytecodeGeneratorCommand<*>>): ProvidesPipeline{
    override fun createProducer(name: String, block: suspend BytecodeProductionEngine.() -> Unit) {
        val producer = BytecodeProductionEngine(recipe)
        GlobalScope.launch {
            producer.block()
            producer.commands.forEach {
                println(it.toString())
            }
        }
    }

    override fun createConsumer(name: String, block: suspend BytecodeConsumptionEngine.() -> Unit) {
        TODO("Not yet implemented")
    }

}

@ExperimentalStdlibApi
class BytecodeRecipeEngine :
    MutableBytecodeGeneratorEngine<BytecodeRecipeProvider>(),
    ProvidesChunkCreator by DefaultChunkCommandFactory(),
    ProvidesOpcodeCreator,
    ProvidesExpectation by DefaultExpectationFactoryImpl() {
    override val errorManager: KBitGeneratorErrorManager<*> = KBitGeneratorErrorManager(this)

    override fun createChunk(block: ChunkCommandFactory.()->Unit){
        val factory = DefaultChunkCommandFactory()
        factory.block()
        this.commands.add(factory.build())
    }

    override fun createOpcode(block: OpcodeCommandFactory.() -> Unit) {
        val factory = DefaultOpcodeCommandFactory()
        factory.block()
        this.commands.add(factory.build())
    }

    override fun finalize() = BytecodeRecipeProvider(this.commands.toList())
}