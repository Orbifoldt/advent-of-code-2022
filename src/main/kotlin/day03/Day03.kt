package day03

import ResourceReader


fun main() {
    part1("example-rucksack.txt")
    part1("my-rucksack.txt")
    part2("example-rucksack.txt")
    part2("my-rucksack.txt")
}

fun part1(filename: String): Int =
    ResourceReader.readLines("day03/$filename")
        .map { it.trim().halves().getDuplicate().itemPriority }
        .reduce { a, b -> a + b }.get()
        .also { println("Sum of priorities in rucksack $filename is '$it'.") }


fun String.halves() = take(length / 2).toCharArray() to drop(length / 2).toCharArray()
fun Pair<CharArray, CharArray>.getDuplicate() = first.intersect(second.asList().toSet()).first()
val Char.itemPriority get() = if (this in 'a'..'z') code - 'a'.code + 1 else code - 'A'.code + 27

fun part2(filename: String) =
    ResourceReader.readLines("day03/$filename")
        .map { it.trim().toCharArray().toList() }
        .toList().chunked(3)
        .sumOf { it.intersection().first().itemPriority }
        .also { println("Sum of priorities of the badges in the groups in $filename is '$it'.") }

fun List<Collection<Char>>.intersection() = this.reduce { acc, chars -> acc.intersect(chars.toSet()) }