package recipe.expectations

import BytecodeGeneratorCommand
import recipe.*

/*
 * Expectations:
 *  - xor
 *      - One and one one expected bytecode component to be selected by the producer
 *  - or
 *      - At least one of the expected bytecode components to be selected by the producer
 *  - and
 *      - All of the expected bytecode components must be fulfilled/satisfied
 *  - nand
 *      - Inverse of 'and'; None of the expected bytecode components are allowed
 *  - nor
 *      - All but one expected bytecode components are allowed by the producer
 */

@ExperimentalStdlibApi
interface ProvidesExpectation{
    fun expectXOR(block: ExpectANDCommandFactory.()->Unit)
    fun expectAND(block: ExpectXORCommandFactory.()->Unit)
}

@ExperimentalStdlibApi
internal interface ExpectationFactory: ProvidesChunkCreator,
    ProvidesOpcodeCreator{
    val predicates: ArrayList<BytecodeGeneratorCommand<*>>
}

@ExperimentalStdlibApi
class DefaultExpectationFactoryImpl: ExpectationFactory {
    override val predicates: ArrayList<BytecodeGeneratorCommand<*>> = ArrayList()

    override fun createChunk(block: ChunkCommandFactory.() -> Unit) {
        val chunkCommandFactory = DefaultChunkCommandFactory()
        chunkCommandFactory.block()
        predicates.add(chunkCommandFactory.build())
    }

    override fun createOpcode(block: OpcodeCommandFactory.() -> Unit) {
        val opcodeCommandFactory = OpcodeCommandFactory()
        opcodeCommandFactory.block()
        predicates.add(opcodeCommandFactory.build())
    }

}







