package day11

import ResourceReader
import product
import java.util.LinkedList
import java.util.Queue

fun main() {
    determineMonkeyBusinessLevel("my-monkey-strategy.txt", 20)
    // Naively doing it with longs will lead to an overflow
    // But using BigIntegers instead will make it way too expensive to compute
//    determineMonkeyBusinessLevel("my-monkey-strategy.txt", 10000, 1)

}

fun determineMonkeyBusinessLevel(filename: String, numRounds: Int, worryQuotient: Int = 3): Long {
    val monkeys = readMonkeysFrom(filename)
    repeat(numRounds) { monkeys.doRound(worryQuotient) }

    return monkeys.calculateMonkeyBusinessLevel(numRounds)
}

fun List<Monkey>.calculateMonkeyBusinessLevel(numRounds: Int): Long {
    val monkeyBusinessLevel = this.map { it.numberOfInspects.toLong() }
        .sortedDescending()
        .take(2)
        .product()
    println("The monkey business level after $numRounds rounds is $monkeyBusinessLevel")
    return monkeyBusinessLevel
}


fun List<Monkey>.doRound(worryQuotient: Int = 3, verbose: Boolean = false) = apply {
    for (monkey in this) {
        if (verbose) println("Monkey ${monkey.name}:")
        while (monkey.startingItems.isNotEmpty()) {
            var item = monkey.inspectFirstItem(verbose)
            item = monkey.executeOperation(item, verbose)
            item = item.decreaseWorry(worryQuotient, verbose)
            monkey.throwItem(item, this, verbose)
        }
    }
}

private fun Monkey.inspectFirstItem(verbose: Boolean): Long {
    val item = startingItems.remove()
    numberOfInspects++
    if (verbose) println("\tMonkey inspects an item with a worry level of $item.")
    return item
}

private fun Long.decreaseWorry(worryQuotient: Int, verbose: Boolean): Long {
    val newWorry = this / worryQuotient.toLong()
    if (verbose) println("\t\tMonkey gets bored with item. Worry level is divided by 3 to $newWorry.")
    return newWorry
}

private fun Monkey.executeOperation(input: Long, verbose: Boolean): Long {
    return when {
        operation == "old * old"         -> {
            if (verbose) println("\t\tWorry level is multiplied by itself to ${input * input}.")
            input * input
        }
        multiplyRegex.matches(operation) -> {
            val multiplier = multiplyRegex.matchEntire(operation)!!.groups[1]!!.value.toLong()
            if (verbose) println("\t\tWorry level is multiplied by $multiplier to ${multiplier * input}.")
            multiplier * input
        }
        sumRegex.matches(operation)      -> {
            val addition = sumRegex.matchEntire(operation)!!.groups[1]!!.value.toLong()
            if (verbose) println("\t\tWorry level is increased by $addition to ${addition + input}.")
            addition + input
        }
        else                             -> throw IllegalArgumentException("Operation '$operation' is invalid")
    }
}

private fun Monkey.throwItem(item: Long, allMonkeys: List<Monkey>, verbose: Boolean) {
    val testPasses = (item % test == 0L)
    if (verbose) println("\t\tCurrent worry level is ${if (testPasses) "" else "not "}divisible by ${test}.")

    val targetMonkey = if (testPasses) ifTrue else ifFalse
    allMonkeys[targetMonkey].startingItems.add(item)
    if (verbose) println("\t\tItem with worry level $item is thrown to monkey $targetMonkey.")

}

data class Monkey(
    val name: Int,
    val startingItems: Queue<Long>,
    val operation: String,
    val test: Long,
    val ifTrue: Int,
    val ifFalse: Int,
    var numberOfInspects: Int = 0,
)

fun readMonkeysFrom(filename: String) = ResourceReader.readString("day11/$filename")
    .split("\\n\\s?\\n".toRegex())
    .map { createMonkeyFrom(it.trim()) }

private fun createMonkeyFrom(string: String): Monkey =
    Regex("""
        Monkey (\d):\s?
        \s*Starting items: ([\d+,\s]+)\s?
        \s*Operation: new = (.*)\s?
        \s*Test: divisible by (\d*)\s?
        \s*If true: throw to monkey (\d)\s?
        \s*If false: throw to monkey (\d).*
        """.trimIndent())
        .matchEntire(string)
        ?.let { matchResult ->
            matchResult.groups.run {
                Monkey(
                    name = get(1)!!.value.toInt(),
                    startingItems = LinkedList(get(2)!!.value.split(",").map { it.trim().toLong() }),
                    operation = get(3)!!.value,
                    test = get(4)!!.value.toLong(),
                    ifTrue = get(5)!!.value.toInt(),
                    ifFalse = get(6)!!.value.toInt(),
                )
            }
        } ?: throw IllegalArgumentException("Bad input:\n'$string'")

val multiplyRegex = "old \\* (\\d+)".toRegex()
val sumRegex = "old \\+ (\\d+)".toRegex()

