package day13

import ResourceReader
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

fun main() {
    part1("example-packets.txt")
    part1("my-packets.txt")
    part2("example-packets.txt")
    part2("my-packets.txt")
}

fun part1(filename: String) = ResourceReader.readGrouped("day13/$filename")
    .map { group -> group.lines().map(Json::parseToJsonElement) }
    .mapIndexed { i, (a, b) ->
        (i + 1) to PacketComparator.compare(a, b)
    }.filter { it.second <= 0 }
    .let { correctlyOrderedPairs ->
        println("The sum of correctly ordered indices is ${correctlyOrderedPairs.sumOf { it.first }}")
    }

object PacketComparator : Comparator<JsonElement> {
    override fun compare(a: JsonElement, b: JsonElement): Int = when {
        a is JsonArray && b is JsonArray         ->
            a.zip(b).map { (x, y) -> compare(x, y) }.firstOrNull { it != 0 } ?: a.size.compareTo(b.size)
        a is JsonPrimitive && b is JsonPrimitive -> a.content.toInt().compareTo(b.content.toInt())
        a is JsonPrimitive && b is JsonArray     -> compare(JsonArray(listOf(a)), b)
        a is JsonArray && b is JsonPrimitive     -> compare(a, JsonArray(listOf(b)))
        else                                     ->
            throw IllegalArgumentException("Input elements should either be JsonArray or JsonPrimitive")
    }
}

const val packet2 = "[[2]]"
const val packet6 = "[[6]]"

fun part2(filename: String) =
    (ResourceReader.readLines("day13/$filename").filter { it.isNotBlank() }.toList() + listOf(packet2, packet6))
        .map(Json::parseToJsonElement)
        .sortedWith(PacketComparator)
        .let { sortedPackets ->
            val idx2 = sortedPackets.withIndex().find { it.value.toString() == packet2 }!!.index + 1
            val idx6 = sortedPackets.withIndex().find { it.value.toString() == packet6 }!!.index + 1
            idx2 * idx6
        }.also { println("The decoder key is $it") }
