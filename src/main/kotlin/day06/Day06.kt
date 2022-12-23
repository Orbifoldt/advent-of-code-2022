package day06

import ResourceReader

fun main() {
    part1("example-datastream.txt")
    part1("example-datastream2.txt")
    part1("my-datastream.txt")
    part2("example-datastream.txt")
    part2("example-datastream2.txt")
    part2("my-datastream.txt")
}

private fun noDoubles(c: List<Char>): Boolean = c.size == 1 || c.first() !in c.drop(1) && noDoubles(c.drop(1))

private fun findStartIndexOfMarker(filename: String, numDistinctChars: Int = 4) =
    ResourceReader.readString("day06/$filename").trim().toList()
        .windowed(numDistinctChars).withIndex()
        .find { (_, chars) -> noDoubles(chars) }!!.index + numDistinctChars

fun part1(filename: String) = findStartIndexOfMarker(filename)
        .also { println("In stream '$filename' the message starts at index $it") }

fun part2(filename: String) = findStartIndexOfMarker(filename, 14)
    .also { println("In stream '$filename' the message starts at index $it") }