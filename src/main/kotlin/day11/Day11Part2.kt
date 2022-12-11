package day11

import product

fun main() {
    determineMonkeyBusinessLevelModulusOnly("my-monkey-strategy.txt", 10000)
}

fun List<Monkey>.getModulus() = map { it.test }.product()  // LCM would suffice instead of just product, but in input all values are primes, so it doesn't matter anyway

fun determineMonkeyBusinessLevelModulusOnly(filename: String, numRounds: Int): Long {
    val monkeys = readMonkeysFrom(filename)
    val modulus = monkeys.getModulus()
    repeat(numRounds) { monkeys.doRoundModulusOnly(modulus) }

    return monkeys.calculateMonkeyBusinessLevel(numRounds)
}

fun List<Monkey>.doRoundModulusOnly(modulus: Long) = apply {
    for (monkey in this) {
        while (monkey.startingItems.isNotEmpty()) {
            var item = monkey.inspectFirstItem()
            item = monkey.executeOperationModulusOnly(item, modulus)
            // No more worry decrease
            monkey.throwItem(item, this)
        }
    }
}

private fun Monkey.inspectFirstItem(): Long {
    numberOfInspects++
    return startingItems.remove()
}

private fun Monkey.executeOperationModulusOnly(input: Long, modulus: Long): Long = when {
    operation == "old * old"         -> input * input
    multiplyRegex.matches(operation) -> multiplyRegex.matchEntire(operation)!!.groups[1]!!.value.toLong() * input
    sumRegex.matches(operation)      -> sumRegex.matchEntire(operation)!!.groups[1]!!.value.toLong() + input
    else                             -> throw IllegalArgumentException("Operation '$operation' is invalid")
} % modulus  // <= Note the modulus here!

private fun Monkey.throwItem(item: Long, allMonkeys: List<Monkey>) {
    val testPasses = (item % test == 0L)
    val targetMonkey = if (testPasses) ifTrue else ifFalse
    allMonkeys[targetMonkey].startingItems.add(item)
}


