package day04

import ResourceReader

fun main() {
    part1("example-schedule.txt")
    part1("my-schedule.txt")
    part2("example-schedule.txt")
    part2("my-schedule.txt")
}

fun part1(filename: String) = readPairsAndCount(filename) { (r1, r2) -> r1 in r2 || r2 in r1 }
    .also { println("There are '$it' pairs in $filename where one range contains the other.") }

private fun readPairsAndCount(filename: String, predicate: (Pair<IntRange, IntRange>) -> Boolean) =
    ResourceReader.readLines("day04/$filename")
        .map { it.split('-', ',').let { (a, b, c, d) -> a.toInt()..b.toInt() to c.toInt()..d.toInt() } }
        .filter(predicate).count()

operator fun IntRange.contains(other: IntRange) = this.first <= other.first && this.last >= other.last

fun part2(filename: String) = readPairsAndCount(filename) { (r1, r2) -> r1 overlapsWith r2 }
    .also { println("There are '$it' pairs in $filename that overlap.") }

infix fun IntRange.overlapsWith(other: IntRange) = this.any { it in other }