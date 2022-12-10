package day10

import ResourceReader

fun main() {
    executeProgram_v2("example-program.txt")
    executeProgram_v2("example-program2.txt")
    executeProgram_v2("my-program.txt")
}

fun executeProgram_v2(filename: String, probeStartCycle: Int = 20, probePeriod: Int = 40): Int {
    println("=====================================\nExecuting '$filename'...")

    val xIncrements = ResourceReader.readLines("day10/$filename").toList().flatMap {
        if (it.startsWith("addx")) listOf(0, it.substring(5).toInt()) else listOf(0)
    } // every "addx n" we replace with [0, n], and noop becomes [0] => each value then corresponds to a cycle

    var exitSignalStrength = 0
    var x = 1
    xIncrements.withIndex().forEach { (cycle, xIncrement) ->
        if ((cycle + 1) % probePeriod == probeStartCycle) exitSignalStrength += (cycle + 1) * x

        print(if ((cycle % probePeriod) in (x - 1)..(x + 1)) "#" else ".")
        if ((cycle + 1) % probePeriod == 0) println()

        x += xIncrement
    }

    println("Program finished with exit signal strength sum $exitSignalStrength")
    return exitSignalStrength
}

