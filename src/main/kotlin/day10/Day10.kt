package day10

import ResourceReader

fun main() {
    executeProgram("example-program.txt")
    executeProgram("example-program2.txt")
    executeProgram("my-program.txt")
}

enum class Operation(val cycles: Int) {
    NOOP(1), ADDX(2)
}

// Very ugly, but it works
fun executeProgram(filename: String, probeStartCycle: Int = 20, probePeriod: Int = 40): Int {
    var exitSignalStrength = 0

    println("=====================================\nExecuting '$filename'...")
    val programLines = ResourceReader.readLines("day10/$filename").iterator()

    var cycle = 0
    var x = 1
    var isRunning = true
    var isBusy = false
    var op: Operation = Operation.NOOP
    var args: List<String> = emptyList()

    while (isRunning) {
        if(isSpriteVisible(x, (cycle) % 40)){
            print("#")
        } else {
            print(".")
        }
        if((cycle) % 40 == 39) println()


        cycle++
        if ((cycle - probeStartCycle) % probePeriod == 0) {
            exitSignalStrength += cycle * x
//            println("During cycle #${cycle} : operation=$op x=$x")
        }

        if (!isBusy) {
            val (nextOp, nextArgs) = parseLine(programLines.next())
            op = nextOp
            args = nextArgs
            when (op) {
                Operation.NOOP -> {}
                Operation.ADDX -> {
                    isBusy = true
                }
            }
        } else {
            when (op) {
                Operation.NOOP -> throw IllegalStateException("NOOP should only take 1 cycle")
                Operation.ADDX -> {
                    x += args[0].toInt()
                    isBusy = false
                }
            }
        }


        if (!isBusy && !programLines.hasNext()) {
            isRunning = false
        }
    }

    println("Program finished with exit signal strength sum $exitSignalStrength")
    return exitSignalStrength
}

fun <T> List<T>.take1() = first() to drop(1)

fun parseLine(line: String): Pair<Operation, List<String>> {
    val (op, args) = line.split(" ").take1()
    return Operation.valueOf(op.uppercase()) to args
}

fun isSpriteVisible(spriteCenterPixel: Int, currentPixel: Int) =
    currentPixel in ((spriteCenterPixel - 1)..(spriteCenterPixel + 1))
