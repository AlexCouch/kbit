class InputCommand{

}

@ExperimentalStdlibApi
abstract class InputEngine(private val generatorEngine: ImmutableBytecodeGeneratorEngine){
    fun getChunk(name: String){
        val chunk = generatorEngine.commands.find { it.name == name } ?: throw IllegalArgumentException("Attempted to get chunk with name $name but it doesn't exist")
    }
}