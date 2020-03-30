# KBit: An easier way to create, maintain, and reuse bytecode
KBit is an engine and kotlin dsl that provides an easier way for creating, maintaining, and reusing bytecode and bytecode like data streams.

### Example
```kotlin
//Call a top level function called `setupBytecode` which will allow you to create your bytecode recipes
//This function will return a built version of the bytecode generator
val engine = setupBytecode{
    //Create a new bytecode chunk recipe
    createChunk{
        this name "example_chunk"
        this describe "An example chunk"
        createOpcode{
            this name "example_opcode"
            this describe "An example opcode for example_chunk"
            this code 0x21
        }
        //Create an expectation for the `input application`
        expect{
            this name "test_expect"
            this describe "This is an example of an enum expectation, where one of many provided opcodes or chunks must be used by the `input app`"
            createOpcode{
                this name "another_example_opcode"
                this describe "This is another example opcode"
                this code 0x22
            }
            createOpcode{
                this name "yet_another_opcode"
                this describe "This is just to prove a point"
                this code 0x23
            }
        }
    }
}

//This will use the built engine to build a byte packet using the provided recipe above
val stream = engine.buildBytePacket{
    //This will get a chunk from the bytecode recipe using the given name. If it doesn't exist, then it will throw an exception
    getChunk("example_chunk"){
        //If the chunk has an expectation like our example does, it is required that the `input app` 'fulfills' this expectation
        //This function lets you fulfill an expectation
        fulfill("test_expect"){
            //Fulfills the expectation by getting an opcode that the expectation provides
            getOpcode("another_example_opcode")
            //This will throw an exception because the expectation has already been fulfilled by "another_example_opcode"
            //getOpcode("yet_another_opcode")
        }
    }
}
```
The above code will generate a bytestream like this: `0x21 0x22` or `33 34`