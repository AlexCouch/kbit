import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@ExperimentalStdlibApi
fun main(){
    val engine = setupBytecode {
        createChunk{
            name = "test"
            description = "A test chunk"
            createOpcode {
                name = "OP_TEST"
                description = "A test opcode"
                code = 0x11
            }
            createOpcode {
                name = "ANOTHER_TEST"
                description = "Another test opcode"
                code = 0x12
            }
        }

        createChunk{
            name = "another_test"
            description = "A test chunk"
            expectXOR {
                name = "test_expect"
                description = "A test expectation"
                createOpcode {
                    name = "OP_TEST"
                    description = "A test opcode"
                    code = 0x21
                }
                createOpcode {
                    name = "ANOTHER_TEST"
                    description = "Another test opcode"
                    code = 0x22
                }
            }
        }
    }
    GlobalScope.launch {
        val stream = engine.createProducer("") {
            this.getChunk("test")
            this.getChunk("another_test"){
                fulfillXor("test_expect"){
                    getOpcode("OP_TEST")
                    getOpcode("ANOTHER_TEST")
                }
            }
        }
    }
    /*while(stream.canRead()){
        print("${stream.readByte()} ")
    }*/
    /*
    engine.buildBytecode{
        this.getChunk("
    }
     */
}