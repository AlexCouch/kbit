package recipe.expectations

import BytecodeCommandFactory
import BytecodeGeneratorCommand
import ImplBytecodeCommandFactory
import recipe.*

/*
 * Expectations:
 *  - xor
 *      - One and only one expected bytecode component to be selected by the producer
 *  - or
 *      - At least one of the expected bytecode components to be selected by the producer
 *  - and
 *      - All of the expected bytecode components must be fulfilled/satisfied
 *  - nand
 *      - Inverse of 'and'; None of the expected bytecode components are allowed
 *  - nor
 *      - All but one expected bytecode components must be provided by the producer
 */

@ExperimentalStdlibApi
interface ProvidesExpectation{
    fun expectXOR(block: ExpectXORCommandFactory.()->Unit)
    fun expectAND(block: ExpectANDCommandFactory.()->Unit)
    fun expectOR(block: ExpectORCommandFactory.()->Unit)
    fun expectNAND(block: ExpectNANDCommandFactory.()->Unit)
    fun expectNOR(block: ExpectNORCommandFactory.()->Unit)
}

@ExperimentalStdlibApi
interface ExpectationFactory:
    ProvidesChunkCreator,
    ProvidesOpcodeCreator,
    ProvidesExpectation{
    val predicates: ArrayList<BytecodeGeneratorCommand<*>>
}

@ExperimentalStdlibApi
class DefaultExpectationFactoryImpl:
    BytecodeCommandFactory<BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand> by ImplBytecodeCommandFactory(),
    ExpectationFactory {
    override val predicates: ArrayList<BytecodeGeneratorCommand<*>> = arrayListOf()
//    override val predicates: ArrayList<BytecodeGeneratorCommand<*>> = arrayListOf()

    override fun createChunk(block: ChunkCommandFactory.() -> Unit) {
        val chunkCommandFactory = DefaultChunkCommandFactory()
        chunkCommandFactory.block()
        predicates.add(chunkCommandFactory.build())
    }

    override fun createOpcode(block: OpcodeCommandFactory.() -> Unit) {
        val opcodeCommandFactory = DefaultOpcodeCommandFactory()
        opcodeCommandFactory.block()
        predicates.add(opcodeCommandFactory.build())
    }

    override fun expectXOR(block: ExpectXORCommandFactory.()->Unit){
        val factory = ExpectXORCommandFactory()
        factory.block()
        this.predicates.add(factory.build())
    }

    override fun expectAND(block: ExpectANDCommandFactory.() -> Unit) {
        val factory = ExpectANDCommandFactory()
        factory.block()
        this.predicates.add(factory.build())
    }

    override fun expectOR(block: ExpectORCommandFactory.() -> Unit) {
        val factory = ExpectORCommandFactory()
        factory.block()
        this.predicates.add(factory.build())
    }

    override fun expectNAND(block: ExpectNANDCommandFactory.() -> Unit) {
        val factory = ExpectNANDCommandFactory()
        factory.block()
        this.predicates.add(factory.build())
    }

    override fun expectNOR(block: ExpectNORCommandFactory.() -> Unit) {
        val factory = ExpectNORCommandFactory()
        factory.block()
        this.predicates.add(factory.build())
    }

}







