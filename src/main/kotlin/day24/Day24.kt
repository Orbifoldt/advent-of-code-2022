@file:OptIn(ExperimentalStdlibApi::class)

package day24

import MultiMap
import ResourceReader
import lcm
import toMultiMap
import java.awt.Point
import java.util.LinkedHashSet

fun main() {
    part1("example-valley.txt")
    part1("my-valley.txt")
    part2("example-valley.txt")
    part2("my-valley.txt")
}

enum class Wind(val char: Char, val x: Int, val y: Int) { N('^', 0, -1), E('>', 1, 0), S('v', 0, 1), W('<', -1, 0); }

private fun windFromChar(c: Char) = Wind.values().first { it.char == c }

private data class Valley(val width: Int, val height: Int, val blizzards: Map<Int, MultiMap<Int, Wind>> = mapOf()) {
    val start = Point(0, -1)
    val end = Point(width - 1, height)
    fun canBeAt(p: Point) = p == start || p == end || (p.x in 0..<width && p.y in 0..<height && get(p).isEmpty())
    operator fun get(p: Point) = blizzards[p.y]?.get(p.x) ?: emptyList()
    fun valleyString() = buildString {
        append("#.")
        repeat(width) { append('#') }
        appendLine()
        (0..<height).forEach { y ->
            append('#')
            (0..<width).forEach { x ->
                val bzs = this@Valley.blizzards[y]?.get(x) ?: emptyList()
                append(if (bzs.isEmpty()) '.' else if (bzs.size == 1) bzs[0].char else bzs.size)
            }
            appendLine('#')
        }
        repeat(width) { append('#') }
        append(".#")
    }
}

private fun Valley.tick(): Valley = this.copy(
    blizzards = blizzards.flatMap { (y, row) ->
        row.flatMap { (x, bz) -> bz.map { (y + it.y).mod(height) to ((x + it.x).mod(width) to it) } }
    }
        .toMultiMap()
        .mapValues { (_, vals) -> vals.toMultiMap() }
)

private fun parseToValley(filename: String): Valley {
    val strings = ResourceReader.readLines("day24/$filename").toList().drop(1).dropLast(1)
    val (height, width) = strings.size to strings[0].length - 2
    return strings.mapIndexed { y, line ->
        y to line.toCharArray().drop(1).dropLast(1).mapIndexed { x, c ->
            x to if (c == '.') mutableListOf() else mutableListOf(
                windFromChar(c)
            )
        }.toMap().toMutableMap()
    }.toMap().let {
        Valley(width, height, it)
    }
}

/**
 * Cache for the valley at different points in time, optimized using cyclic nature of the blizzards
 */
private data class ValleyStates(val initial: Valley) {
    private val knownStates = mutableListOf(initial)
    private val lcm = lcm(initial.width, initial.height)
    private fun addNextState() = knownStates.add(knownStates.last().tick())
    operator fun get(i: Int): Valley {
        val index = i.mod(lcm)
        if (index >= knownStates.size) repeat(index - knownStates.size + 1) { addNextState() }
        return knownStates[index]
    }
}

fun part1(filename: String) {
    val valleys = ValleyStates(parseToValley(filename))
    val t = goToEnd(valleys, 1)
    println("Reached end in $t minutes")
}

private fun goToEnd(valleys: ValleyStates, startTime: Int) =
    goTo(valleys, startTime, valleys[0].start, valleys[0].end)

private fun goTo(valleys: ValleyStates, startTime: Int, from: Point, to: Point): Int {
    var t = startTime
    val q: LinkedHashSet<Point> = LinkedHashSet(listOf(from))
    while (true) {
        val nextPositions = mutableListOf<Point>()

        while (q.isNotEmpty()) {
            val current = q.first().also { q.remove(it) }
            if (current == to) return t
            current.nextPositions().forEach { p -> if (valleys[t + 1].canBeAt(p)) nextPositions.add(p) }
        }
        q.addAll(nextPositions)
        t++
    }
}

private fun Point.nextPositions() =
    listOf(Point(x, y - 1), Point(x - 1, y), Point(x, y), Point(x + 1, y), Point(x, y + 1))


fun part2(filename: String) {
    val valleys = ValleyStates(parseToValley(filename))

    val t = goToEnd(valleys, 1)
    val t2 = goToStart(valleys, t)
    val t3 = goToEnd(valleys, t2)
    println("Got to end, then start, and then end again in $t3 minutes")
}

private fun goToStart(valleys: ValleyStates, startTime: Int): Int =
    goTo(valleys, startTime, valleys[0].end, valleys[0].start)
