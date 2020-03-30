object ErrorHandler {
    fun reportError(block: PrettyPrinter.()->Unit){
        val message = buildPrettyString {
            this.append("\u001B[31m")
            this.block()
        }
        println(message)
        print("\u001B[0m")
    }
}