package day14

import ResourceReader

fun main() {
    runSimulation("example-rocks.txt")
    runSimulation("my-rocks.txt", shiftX = 450)
    runSimulation("example-rocks.txt", part2 = true)
    runSimulation("my-rocks.txt", part2 = true, shiftX = 330)
}

internal typealias RockMap = List<MutableList<Char>>

internal typealias Coord = Pair<Int, Int>

internal val Coord.x get() = first
internal val Coord.y get() = second

private val SPAWN = 500 to 0

internal operator fun RockMap.get(coord: Coord) = this[coord.y][coord.x]
internal operator fun RockMap.set(coord: Coord, value: Char) {
    this[coord.y][coord.x] = value
}


fun runSimulation(filename: String, part2: Boolean = false, shiftX: Int = 487) {
    val map = createMap(filename, withFloor = part2)

    var n = 0
    while (true) {
        if (map.dropSand()) {
            n++
        } else {
            break
        }
    }

    println("Final state for $filename${if (part2) " with a floor" else ""}:")
    if (!part2) map[SPAWN] = '+'
    map.forEach { println(it.drop(shiftX).joinToString("")) }
    println("Could drop a total of $n grains of sand\n")
}


fun RockMap.dropSand(spawn: Coord = SPAWN): Boolean {
    var current: Coord = spawn
    while (true) {
        if (this[spawn] == 'o') {
            return false // sand spawn is covered
        }

        val outcome = try {
            this.nextSandCoord(current)
        } catch (e: IndexOutOfBoundsException) {
            return false // sand is in free fall
        }
        if (outcome == null) {
            this[current] = 'o'
            return true // sand has come to a rest
        }
        current = outcome
    }
}

fun Coord.increment(x: Int = 0, y: Int = 0) = copy(first = first + x, second = second + y)

fun RockMap.nextSandCoord(current: Coord) = when {
    this[current.increment(y = 1)] == '.'         -> current.increment(y = 1)
    this[current.increment(x = -1, y = 1)] == '.' -> current.increment(x = -1, y = 1)
    this[current.increment(x = 1, y = 1)] == '.'  -> current.increment(x = 1, y = 1)
    else                                          -> null
}


fun createMap(filename: String, withFloor: Boolean = false) =
    ResourceReader.readLines("day14/$filename").toList()
        .map { line ->
            line.split(" -> ").map { coordString ->
                coordString.split(",").let { (a, b) -> (a.toInt()) to b.toInt() }
            }
        }.let {
            val height = it.maxY + 2
            if (withFloor) it + listOf(listOf((-height + SPAWN.x) to height, (height + SPAWN.x) to height)) else it
        }
        .let { rockFormations ->
            println("max Y: ${rockFormations.maxY}, max x ${rockFormations.maxX}")
            List(rockFormations.maxY + 2) { MutableList(rockFormations.maxX + 2) { '.' } }
                .drawRockLines(rockFormations)
        }

val List<List<Coord>>.maxX get() = flatten().maxBy { it.x }.x
val List<List<Coord>>.maxY get() = flatten().maxBy { it.y }.y

fun RockMap.drawRockLines(rockFormations: List<List<Coord>>) = apply {
    rockFormations.forEach { line ->
        line.zipWithNext().map { (a, b) -> this.drawLine(a, b) }
    }
}

fun RockMap.drawLine(a: Coord, b: Coord) =
    when {
        a.x == b.x -> range(a.y, b.y).map { y -> this[a.x to y] = '#' }
        a.y == b.y -> range(a.x, b.x).map { x -> this[x to a.y] = '#' }
        else       -> throw IllegalArgumentException("Line should be horizontal, but got $a and $b.")
    }

fun range(m: Int, n: Int) = if (m <= n) m..n else n..m