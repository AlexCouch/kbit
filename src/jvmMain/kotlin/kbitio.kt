@ExperimentalStdlibApi
fun main(){
    val engine = setupBytecode {
        createChunk{
            this name "test"
            this describe "A test chunk"
            createOpcode {
                this name "OP_TEST"
                this describe "A test opcode"
                this code 0x11
            }
            createOpcode {
                this name "ANOTHER_TEST"
                this describe "Another test opcode"
                this code 0x12
            }
            /*
            expect{

            }
             */
        }

        createChunk{
            this name "another_test"
            this describe "A test chunk"
            expect {
                this name "test_expect"
                this describe "A test expectation"
                createOpcode {
                    this name "OP_TEST"
                    this describe "A test opcode"
                    this code 0x21
                }
                createOpcode {
                    this name "ANOTHER_TEST"
                    this describe "Another test opcode"
                    this code 0x22
                }
            }
        }
    }
    val stream = engine.buildBytePacket {
        this.getChunk("test")
        this.getChunk("another_test"){
            fulfill("test_expect"){
                getOpcode("OP_TEST")
            }
        }
    }
    while(stream.canRead()){
        print("${stream.readByte()} ")
    }
    /*
    engine.buildBytecode{
        this.getChunk("
    }
     */
}