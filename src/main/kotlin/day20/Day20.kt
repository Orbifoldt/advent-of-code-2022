package day20

import ResourceReader
import java.util.LinkedList
import java.util.stream.Stream

fun main() {
    part1("example-code.txt")
    part1("my-code.txt")
    part2("example-code.txt")
    part2("my-code.txt")
}

fun part1(filename: String) {
    val encrypted = mixing(readCode(filename).toList())
    println("Coordinate for $filename is '${encrypted.getCoordinate()}'")
}

private fun readCode(filename: String) = ResourceReader.readLines("day20/$filename").map { NumberWrapper(it.toLong()) }

class NumberWrapper(val value: Long) {  // Creating a wrapper with an equals method that only checks object ref
    override fun equals(other: Any?) = this === other  // actually not necessary to override, but added to be explicit
    override fun hashCode() = javaClass.hashCode()  // to stop IntelliJ from complaining
    override fun toString() = "$value"  // just for pretty printing
}

// This actually doesn't wrap around as shown, but it does produce the same order of all elements, and since we are
// only interested at position relative to 0 we don't care. (it inserts 0 in front, but it should be at the back)
// Also, we use Math.floorMod to ensure the result is always between 0 and the modulus - 1 (% can give negative results)
private fun <E> LinkedList<E>.insertAt(index: Long, value: E) = add(Math.floorMod(index, size), value)

fun List<NumberWrapper>.getCoordinate(): Long {
    val zeroIndex = this.withIndex().find { it.value.value == 0L }!!.index

    fun <E> List<E>.getModulo(index: Int) = get(Math.floorMod(index, size))
    return getModulo(zeroIndex + 1000).value +
            getModulo(zeroIndex + 2000).value +
            getModulo(zeroIndex + 3000).value
}

fun mixing(initial: List<NumberWrapper>, rounds: Int = 1): List<NumberWrapper> {
    val output = LinkedList(initial)
    repeat(rounds){
        for (num in initial) {
            val currentIndex = output.indexOf(num)
            output.remove(num)
            output.insertAt(currentIndex + num.value, num)
        }
    }
    return output.toList()
}

fun part2(filename: String) {
    val encrypted = mixing(readCode(filename).applyDecryptionKey().toList(), rounds = 10)
    println("Coordinate for $filename is '${encrypted.getCoordinate()}'")
}

private fun Stream<NumberWrapper>.applyDecryptionKey(key: Int = 811589153) = map { NumberWrapper(it.value * key )}