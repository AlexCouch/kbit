import consumption.BytecodeConsumptionEngine
import production.*
import recipe.BytecodeRecipeEngine
import recipe.BytecodeRecipeProvider
import results.ErrorResult
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList
import results.*

sealed class BytecodeGeneratorCommand<T>(open val name: String, open val description: String) where T : BytecodeComponent {
    abstract fun toComponent(): Result<T>
    sealed class CreateCommand<T>(override val name: String, override val description: String) : BytecodeGeneratorCommand<T>(name, description) where T : BytecodeComponent {
        data class CreateOpcodeCommand(override val name: String, override val description: String, val code: Byte) :
            CreateCommand<Opcode>(name, description) {
            override fun toString(): String {
                return buildPrettyString {
                    this.appendWithNewLine("production.Opcode{")
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
            val components: ArrayDeque<BytecodeGeneratorCommand<*>>
        ) : CreateCommand<Chunk>(name, description) {
            override fun toString(): String {
                return buildPrettyString {
                    this.appendWithNewLine("production.Chunk{")
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
                            when(val result = component.toComponent()){
                                is WrappedResult -> newcomponents.add(result.t)
                                is ErrorResult -> return ErrorResult("An error occurred while converting expectation command to bytecode component", result)
                                else -> return ErrorResult("Unrecognized expectation fulfillment command: $component")
                            }
                        }
                    }
                }
                return WrappedResult(
                    Chunk(
                        this.name,
                        this.description,
                        ArrayDeque(newcomponents)
                    )
                )
            }

        }

    }

    sealed class ExpectCommand<T : Expectation>(
        override val name: String,
        override val description: String
    ) : BytecodeGeneratorCommand<T>(name, description){
        internal var fulfilled = false

        data class ExpectANDCommand(
            override val name: String,
            override val description: String,
            val choices: ArrayList<BytecodeGeneratorCommand<*>>
        ): ExpectCommand<Expectation.ANDExpectation>(name, description){
            override fun toComponent(): Result<Expectation.ANDExpectation> {
                TODO("Not yet implemented")
            }
            override fun toString(): String = buildPrettyString {
                this.appendWithNewLine("ANDExpect{")
                this.indent {
                    this.appendWithNewLine("Name: $name")
                    this.appendWithNewLine("Description: $description")
                    this.appendWithNewLine("Choices: [")
                    this.indent {
                        choices.forEach{
                            this.appendWithNewLine("$it{")
                            this.appendWithNewLine("Fulfilled: $fulfilled")
                            this.appendWithNewLine("}")
                        }
                    }
                    this.appendWithNewLine("]")
                }
                this.append("}")
            }
        }
        data class ExpectXORCommand(
            override val name: String,
            override val description: String,
            val cases: ArrayList<BytecodeGeneratorCommand<*>>
        ): ExpectCommand<Expectation.XORExpectation>(name, description){
            internal var fulfilledCommand: BytecodeGeneratorCommand<*>? = null
            override fun toString(): String = buildPrettyString {
                this.appendWithNewLine("XORExpect{")
                this.indent {
                    this.appendWithNewLine("Name: $name")
                    this.appendWithNewLine("Description: $description")
                    this.appendWithNewLine("Choices: [")
                    this.indent {
                        cases.forEach{
                            this.appendWithNewLine(it.toString())
                        }
                    }
                    this.appendWithNewLine("]")
                    this.appendWithNewLine("Fulfilled: $fulfilledCommand")
                }
                this.append("}")
            }

            override fun toComponent(): Result<Expectation.XORExpectation>{
                if(this.fulfilledCommand == null) return ErrorResult("XOR production.Expectation has not bot been fulfilled: $name")
                val comp = when(val result = this.fulfilledCommand?.toComponent()){
                    is WrappedResult -> result.t
                    is ErrorResult -> return ErrorResult("An error occurred while converting XOR production.Expectation command to XOR expectation component")
                    else -> return ErrorResult("Unrecognized result: $result")
                }
                return WrappedResult(Expectation.XORExpectation(this.name, this.description, comp))
            }
        }
        data class ExpectNORCommand(
            override val name: String,
            override val description: String,
            val cases: ArrayList<BytecodeGeneratorCommand<*>>
        ): ExpectCommand<Expectation.NORExpectation>(name, description){
            internal var fulfilledCommands: ArrayList<BytecodeGeneratorCommand<*>> = arrayListOf()
            override fun toString(): String = buildPrettyString {
                this.appendWithNewLine("NORExpect{")
                this.indent {
                    this.appendWithNewLine("Name: $name")
                    this.appendWithNewLine("Description: $description")
                    this.appendWithNewLine("Cases: [")
                    this.indent {
                        cases.forEach{
                            this.appendWithNewLine(it.toString())
                        }
                    }
                    this.appendWithNewLine("]")
                    this.appendWithNewLine("Fulfilled{")
                    this.indent {
                        fulfilledCommands.forEach {
                            this.appendWithNewLine(it.toString())
                        }
                    }
                    this.appendWithNewLine("}")
                }
                this.append("}")
            }

            override fun toComponent(): Result<Expectation.NORExpectation> {
                if(this.fulfilledCommands.isEmpty()) return ErrorResult("NOR production.Expectation has not bot been fulfilled: $name")
                val comps = fulfilledCommands.map {
                    when(val result = it.toComponent()){
                        is WrappedResult -> result.t
                        is ErrorResult -> return ErrorResult("An error occurred while converting NOR production.Expectation command to NOR expectation component", result)
                        else -> return ErrorResult("Unrecognized result: $result")
                    }
                }
                return WrappedResult(Expectation.NORExpectation(this.name, this.description, comps))
            }
        }
        data class ExpectORCommand(
            override val name: String,
            override val description: String,
            val cases: ArrayList<BytecodeGeneratorCommand<*>>
        ): ExpectCommand<Expectation.ORExpectation>(name, description){
            internal var fulfilledCommands: List<BytecodeGeneratorCommand<*>> = arrayListOf()
            override fun toString(): String = buildPrettyString {
                this.appendWithNewLine("ORExpect{")
                this.indent {
                    this.appendWithNewLine("Name: $name")
                    this.appendWithNewLine("Description: $description")
                    this.appendWithNewLine("Cases: [")
                    this.indent {
                        cases.forEach{
                            this.appendWithNewLine(it.toString())
                        }
                    }
                    this.appendWithNewLine("]")
                    this.appendWithNewLine("Fulfilled{")
                    this.indent {
                        fulfilledCommands.forEach {
                            this.appendWithNewLine(it.toString())
                        }
                    }
                    this.appendWithNewLine("}")
                }
                this.append("}")
            }

            override fun toComponent(): Result<Expectation.ORExpectation> {
                if(this.fulfilledCommands.isEmpty()) return ErrorResult("OR production.Expectation has not bot been fulfilled: $name")
                val comps = fulfilledCommands.map {
                    when(val result = it.toComponent()){
                        is WrappedResult -> result.t
                        is ErrorResult -> return ErrorResult("An error occurred while converting NOR production.Expectation command to OR expectation component", result)
                        else -> return ErrorResult("Unrecognized result: $result")
                    }
                }
                return WrappedResult(Expectation.ORExpectation(this.name, this.description, comps))
            }
        }
        data class ExpectNANDCommand(
            override val name: String,
            override val description: String,
            val cases: ArrayList<BytecodeGeneratorCommand<*>>
        ): ExpectCommand<Expectation.ORExpectation>(name, description) {
            internal var fulfilledCommands: List<BytecodeGeneratorCommand<*>> = arrayListOf()
            override fun toString(): String = buildPrettyString {
                this.appendWithNewLine("NANDExpect{")
                this.indent {
                    this.appendWithNewLine("Name: $name")
                    this.appendWithNewLine("Description: $description")
                    this.appendWithNewLine("Cases: [")
                    this.indent {
                        cases.forEach {
                            this.appendWithNewLine(it.toString())
                        }
                    }
                    this.appendWithNewLine("]")
                    this.appendWithNewLine("Fulfilled{")
                    this.indent {
                        fulfilledCommands.forEach {
                            this.appendWithNewLine(it.toString())
                        }
                    }
                    this.appendWithNewLine("}")
                }
                this.append("}")
            }

            override fun toComponent(): Result<Expectation.ORExpectation> {
                if (this.fulfilledCommands.isEmpty()) return ErrorResult("NAND production.Expectation has not bot been fulfilled: $name")
                val comps = fulfilledCommands.map {
                    when (val result = it.toComponent()) {
                        is WrappedResult -> result.t
                        is ErrorResult -> return ErrorResult(
                            "An error occurred while converting NAND production.Expectation command to NAND expectation component",
                            result
                        )
                        else -> return ErrorResult("Unrecognized result: $result")
                    }
                }
                return WrappedResult(Expectation.ORExpectation(this.name, this.description, comps))
            }
        }
    }
}

@ExperimentalStdlibApi
interface BytecodeCommandFactory<T : BytecodeGeneratorCommand<*>>{
    var name: String
    var description: String

    val errorManager: KBitGeneratorErrorManager<out BytecodeCommandFactory<T>>

    fun build(): T

}

@ExperimentalStdlibApi
class ImplBytecodeCommandFactory<T : BytecodeGeneratorCommand<*>>: BytecodeCommandFactory<T>{
    override var name = ""
    override var description = ""

    override fun build(): T {
        TODO("Not yet implemented")
    }

    override val errorManager = KBitGeneratorErrorManager(this)
}

@ExperimentalStdlibApi
interface ProvidesPipeline{
    fun createProducer(name: String, block: suspend BytecodeProductionEngine.()->Unit)
    fun createConsumer(name: String, block: suspend BytecodeConsumptionEngine.()->Unit)
}

@ExperimentalStdlibApi
abstract class MutableBytecodeGeneratorEngine<T>{
    internal open val commands = ArrayList<BytecodeGeneratorCommand<*>>()
    abstract val errorManager: KBitGeneratorErrorManager<*>
    abstract fun finalize(): T
}

@ExperimentalStdlibApi
abstract class FinalBytecodeGeneratorEngine<T>(internal open val commands: List<BytecodeGeneratorCommand<*>>){
    abstract val errorManager: KBitGeneratorErrorManager<*>
    abstract fun finalize(): T
}

@ExperimentalStdlibApi
fun setupBytecode(block: BytecodeRecipeEngine.()->Unit): BytecodeRecipeProvider {
    val genEngine = BytecodeRecipeEngine()
    genEngine.block()
    genEngine.commands.forEach {
        println(it)
    }
    return genEngine.finalize()
}