import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import results.ErrorResult
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList
import results.*
import kotlin.IllegalStateException

sealed class BytecodeGeneratorCommand(open val name: String, open val description: String) {
    sealed class CreateCommand<T>(override val name: String, override val description: String) : BytecodeGeneratorCommand(name, description) where T : BytecodeComponent {
        abstract fun toComponent(): Result<T>

        data class CreateOpcodeCommand(override val name: String, override val description: String, val code: Byte) :
            CreateCommand<Opcode>(name, description) {
            override fun toString(): String {
                return buildPrettyString {
                    this.appendWithNewLine("Opcode{")
                    this.indent {
                        this.appendWithNewLine("Name: $name")
                        this.appendWithNewLine("Description: $description")
                        this.appendWithNewLine("Code: $code")
                    }
                    this.appendWithNewLine("}")
                }
            }

            override fun toComponent(): Result<Opcode> {
                return WrappedResult(Opcode(this.name, this.description, this.code))
            }
        }

        @ExperimentalStdlibApi
        data class CreateChunkCommand(
            override val name: String,
            override val description: String,
            val components: ArrayDeque<BytecodeGeneratorCommand>
        ) : CreateCommand<Chunk>(name, description) {
            override fun toString(): String {
                return buildPrettyString {
                    this.append("Chunk{\n")
                    this.indent {
                        this.appendWithNewLine("Name: $name")
                        this.appendWithNewLine("Description: $description")
                        this.appendWithNewLine("Composition: [")
                        this.indent {
                            components.forEach {
                                this.appendWithNewLine(it.toString())
                            }
                        }
                        this.appendWithNewLine("]")
                    }
                    this.append("}")
                }
            }

            override fun toComponent(): Result<Chunk> {
                val newcomponents = ArrayList<BytecodeComponent>()
                if(this.components.isEmpty()) return ErrorResult("Cannot built chunk without opcodes")
                for(component in components){
                    when(component){
                        is CreateOpcodeCommand -> {
                            when(val result = component.toComponent()){
                                is WrappedResult<*> -> {
                                    when(result.t){
                                        is Opcode -> newcomponents.add(result.t as Opcode)
                                        else -> return ErrorResult("Did not get bytecode component from CreateOpcodeCommand#toComponent, but instead got ${result.t}")
                                    }
                                }
                                is ErrorResult -> return ErrorResult("An error occurred while converting create opcode command to opcode component", result)
                            }
                        }
                        is CreateChunkCommand -> {
                            val chunk = when(val result = component.toComponent()){
                                is WrappedResult -> {
                                    result.t
                                }
                                is ErrorResult -> return ErrorResult("An error occurred while converting CreateChunkCommand to chunk component", result)
                                else -> return ErrorResult("Unrecognized result: $result")
                            }
                            newcomponents.add(chunk)
                        }
                        is ExpectCommand -> {
                            if(!component.fulfilled){
                                return ErrorResult("Attempted to build bytecode with unfulfilled expect command: $component")
                            }
                            component.fulfilledCommand ?: return ErrorResult("Expectation is marked fulfilled but has no fulfilled command: $component")
                            when(component.fulfilledCommand){
                                is CreateCommand<*> -> {
                                    when(val result = (component.fulfilledCommand as CreateCommand<*>).toComponent()){
                                        is WrappedResult -> newcomponents.add(result.t)
                                        is ErrorResult -> return ErrorResult("An error occurred while converting fulfilled expectation command to bytecode component")
                                    }
                                }
                                else -> return ErrorResult("Unrecognized expectation fulfillment command: $component")
                            }

                        }
                    }
                }
                return WrappedResult(Chunk(this.name, this.description, ArrayDeque(newcomponents)))
            }

        }

    }

    data class ExpectCommand(
        override val name: String,
        override val description: String,
        val predicates: ArrayList<BytecodeGeneratorCommand>
    ) : BytecodeGeneratorCommand(name, description){
        internal var fulfilled = false
        internal var fulfilledCommand: BytecodeGeneratorCommand? = null
            set(new){
                fulfilled = true
                field = new
            }

        override fun toString(): String = buildPrettyString {
            this.appendWithNewLine("ExpectCommand{")
            this.indent {
                this.appendWithNewLine("Name: $name")
                this.appendWithNewLine("Description: $description")
                this.appendWithNewLine("Predicates: [")
                this.indent {
                    predicates.forEach{
                        this.appendWithNewLine(it.toString())
                    }
                }
                this.appendWithNewLine("]")
            }
            this.append("}")
        }
    }
}

internal interface BytecodeCommandFactory<T : BytecodeGeneratorCommand>{
    var name: String
    var description: String

    infix fun name(name: String)
    infix fun describe(description: String)
    fun build(): T

}

class ImplBytecodeCommandFactory<T : BytecodeGeneratorCommand>: BytecodeCommandFactory<T>{
    override var name = ""
    override var description = ""

    override infix fun name(name: String){
        this.name = name
    }

    override infix fun describe(description: String){
        this.description = description
    }

    override fun build(): T {
        TODO("Not yet implemented")
    }
}

@ExperimentalStdlibApi
class ChunkCommandFactory: BytecodeCommandFactory<BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand> by ImplBytecodeCommandFactory(){
    private val children = ArrayDeque<BytecodeGeneratorCommand>()

    fun createOpcode(block: OpcodeCommandFactory.()->Unit){
        val factory = OpcodeCommandFactory()
        factory.block()
        this.children.add(factory.build())
    }

    fun expect(block: ExpectCommandFactory.()->Unit){
        val factory = ExpectCommandFactory()
        factory.block()
        this.children.add(factory.build())
    }

    override fun build():  BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand = BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand(this.name, this.description, this.children)
}

@ExperimentalStdlibApi
class ExpectCommandFactory: BytecodeCommandFactory<BytecodeGeneratorCommand> by ImplBytecodeCommandFactory(){
    private val predicates = ArrayList<BytecodeGeneratorCommand>()

    fun createChunk(block: ChunkCommandFactory.()->Unit){
        val chunkCommandFactory = ChunkCommandFactory()
        chunkCommandFactory.block()
        predicates.add(chunkCommandFactory.build())
    }

    fun createOpcode(block: OpcodeCommandFactory.()->Unit){
        val opcodeCommandFactory = OpcodeCommandFactory()
        opcodeCommandFactory.block()
        predicates.add(opcodeCommandFactory.build())
    }

    override fun build(): BytecodeGeneratorCommand = BytecodeGeneratorCommand.ExpectCommand(name, description, predicates)

}

@ExperimentalStdlibApi
class OpcodeCommandFactory: BytecodeCommandFactory<BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand> by ImplBytecodeCommandFactory(){
    private var code: Byte = -1

    infix fun code(code: Byte){
        this.code = code
    }

    override fun build(): BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand = BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand(this.name, this.description, this.code)
}

@ExperimentalStdlibApi
class ExpectationFulfillmentFactory(private var expectCommand: BytecodeGeneratorCommand.ExpectCommand){
    fun getChunk(name: String){
        val matchedPredicate = expectCommand.predicates.find { it.name == name } ?: throw IllegalArgumentException("No chunk with name $name exists in expectation predicates for ${expectCommand.name}. Either change the name or create a chunk called $name")
        if(matchedPredicate !is BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand) throw IllegalStateException("No chunk found with name $name in predicates for expectation ${expectCommand.name}")
        this.expectCommand.fulfilledCommand = matchedPredicate
    }

    fun getOpcode(name: String){
        val matchedPredicate = expectCommand.predicates.find { it.name == name } ?: throw IllegalArgumentException("No opcode with name $name exists in expectation predicates for ${expectCommand.name}. Either change the name or create a chunk called $name")
        if(matchedPredicate !is BytecodeGeneratorCommand.CreateCommand.CreateOpcodeCommand) throw IllegalStateException("No opcode found with name $name in predicates for expectation ${expectCommand.name}")
        this.expectCommand.fulfilledCommand = matchedPredicate
    }
}

@ExperimentalStdlibApi
class GetChunkFactory(private var chunkCommand: BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand){
    fun fulfill(name: String, block: ExpectationFulfillmentFactory.()->Unit){
        val comp = this.chunkCommand.components.find { it.name == name } ?: throw IllegalArgumentException("There are no commands by the name $name that currently exist. Either change the name to something that exists or make one using this name.")
        if(comp !is BytecodeGeneratorCommand.ExpectCommand) throw IllegalStateException("The found command with name $name is not an expect command, but instead is $comp")
        val expectFulfillment = ExpectationFulfillmentFactory(comp)
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

@ExperimentalStdlibApi
abstract class BytecodeGeneratorEngine<T>{
    internal open val commands = ArrayDeque<BytecodeGeneratorCommand>()
    abstract fun build(): T
}

@ExperimentalStdlibApi
class ImmutableBytecodeGeneratorEngine(override val commands: ArrayDeque<BytecodeGeneratorCommand>): BytecodeGeneratorEngine<ByteReadPacket>(){
    private val bytes = ArrayDeque<BytecodeComponent>()
    private fun findChunk(name: String): BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand{
        val found = this.commands.find { it.name == name } ?: throw IllegalArgumentException("Attempted to get chunk with name $name but it doesn't exist")
        if(found !is BytecodeGeneratorCommand.CreateCommand.CreateChunkCommand) throw IllegalArgumentException("Attempted to get chunk with $name but it is not a chunk but is instead $found")
        return found
    }
    fun getChunk(name: String){
        val comp = findChunk(name)
        val expectations = comp.components.filterIsInstance<BytecodeGeneratorCommand.ExpectCommand>()
        if(expectations.isNotEmpty()){
            for(expectation in expectations){
                if(!expectation.fulfilled){
                    return ErrorHandler.reportError {
                        this.appendWithNewLine("Chunk $name has expectations to be fulfilled:")
                        this.indent {
                            this.append(expectation.toString())
                        }
                    }
                }
            }
        }
        val chunk = when(val result = comp.toComponent()){
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
        bytes.add(chunk)
    }
    fun fulfill(name: String, block: ExpectationFulfillmentFactory.()->Unit){
        val comp = this.commands.find { it.name == name } ?: throw IllegalArgumentException("There are no commands by the name $name that currently exist. Either change the name to something that exists or make one using this name.")
        if(comp !is BytecodeGeneratorCommand.ExpectCommand) throw IllegalStateException("The found command with name $name is not an expect command, but instead is $comp")
        val expectFulfillment = ExpectationFulfillmentFactory(comp)
        expectFulfillment.block()
    }
    fun getChunk(name: String, block: GetChunkFactory.()->Unit){
        val comp = this.findChunk(name)
        val getChunkFactory = GetChunkFactory(comp)
        getChunkFactory.block()
        val chunk = getChunkFactory.build()
        this.bytes.add(chunk)
    }

    override fun build(): ByteReadPacket =buildPacket {
        bytes.forEach {
            this.writePacket(it.toBytePacket())
        }
    }

    fun buildBytePacket(block: ImmutableBytecodeGeneratorEngine.() -> Unit): ByteReadPacket{
        this.block()
        return this.build()
    }
}

@ExperimentalStdlibApi
class MutableBytecodeGeneratorEngine: BytecodeGeneratorEngine<ImmutableBytecodeGeneratorEngine>(){
    fun createChunk(block: ChunkCommandFactory.()->Unit){
        val factory = ChunkCommandFactory()
        factory.block()
        this.commands.add(factory.build())
    }

    fun expect(block: ExpectCommandFactory.()->Unit){
        val factory = ExpectCommandFactory()
        factory.block()
        this.commands.add(factory.build())
    }

    override fun build(): ImmutableBytecodeGeneratorEngine{
        this.commands.forEach{
            println(it.toString())
        }
        return ImmutableBytecodeGeneratorEngine(this.commands)
    }
}

@ExperimentalStdlibApi
fun setupBytecode(block: MutableBytecodeGeneratorEngine.()->Unit): ImmutableBytecodeGeneratorEngine{
    val genEngine = MutableBytecodeGeneratorEngine()
    genEngine.block()
    return genEngine.build()
}