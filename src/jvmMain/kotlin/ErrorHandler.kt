import errormanager.ErrorEntry
import errormanager.ErrorManager

object ErrorHandler {
    fun reportError(block: PrettyPrinter.()->Unit){
        val message = buildPrettyString {
            red{
                this.block()
            }
        }
        println(message)
    }
}

@ExperimentalStdlibApi
class KBitGeneratorErrorManager<T>(override val module: T) : ErrorManager<T>(){
    override suspend fun createError(moduleName: String, message: String) {
        this.errorStream.send(ErrorEntry(moduleName, "$moduleName encountered an error: $message"))
    }
}