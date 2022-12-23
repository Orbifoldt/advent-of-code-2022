package day23

import ResourceReader
import java.awt.Point

fun main() {
//    part1("test2-elves.txt")
    part1("example-elves.txt")
    part1("my-elves.txt")
    part2("example-elves.txt")
    part2("my-elves.txt")
//    part2("example-.txt")
//    part2("my-.txt")
}


enum class Direction(val point: Point) { N(Point(0, -1)), S(Point(0, 1)), W(Point(-1, 0)), E(Point(1, 0)); }
fun getDirections(offset: Int) = (0..4).map { Direction.values()[Math.floorMod(it + offset, 4)]}

operator fun Point.plus(other: Point) = Point(x + other.x, y + other.y)
fun Point.possibleNeighbors() = hashSetOf(
    Point(x - 1, y - 1), Point(x, y - 1), Point(x + 1, y - 1),
    Point(x - 1, y), /*                      */ Point(x + 1, y),
    Point(x - 1, y + 1), Point(x, y + 1), Point(x + 1, y + 1),
)
fun Point.getNeighbors(others: HashSet<Point>) = this.possibleNeighbors().mapNotNull { n -> others.find { it.x == n.x && it.y == n.y } }//others.filter { Point(it.x, it.y) in this.possibleNeighbors() }
fun Point.count(direction: Direction, neighbors: List<Point>) = when (direction) {
    Direction.N, Direction.S -> neighbors.count { it.y == y + direction.point.y }
    Direction.E, Direction.W -> neighbors.count { it.x == x + direction.point.x }
}

fun Set<Point>.boundingBox() = Point(minOf { it.x }, minOf { it.y }) to Point(maxOf { it.x }, maxOf { it.y })
fun Set<Point>.asString() = buildString {
    val (min, max) = this@asString.boundingBox()
    val ptx = this@asString
    (0..max.y - min.y).forEach { y ->
        (0..max.x - min.x).forEach { x ->
            if (Point(x + min.x, y + min.y) in ptx) {
                append('#')
            } else {
                append('.')
            }
        }
        appendLine()
    }
}

fun part1(filename: String) {
    var currentPoints = parse(filename)

    repeat(10) {
        currentPoints = doRound(it, currentPoints).first
//        println("\nEnd of Round ${it + 1}")
//        println(currentPoints.asString())
    }

    val (min, max) = currentPoints.boundingBox()
    val empty = (max.x - min.x + 1) * (max.y - min.y + 1) - currentPoints.size
    println("After 10 rounds there are '$empty' empty tiles")

}

private fun doRound(it: Int, currentPoints: HashSet<Point>): Pair<HashSet<Point>, Boolean> {
    val dirs = getDirections(it)
    val nextPoints = currentPoints.associateWith { p -> nextPosition(p, currentPoints, dirs) }

    val targets = nextPoints.values.groupBy { it }
    var anyMoved = false
    return nextPoints.map { (p, n) ->
        if (p!=n && targets[n]?.size == 1) {
            anyMoved = true
            n
        } else {
            p
        }
    }.toHashSet() to anyMoved
}

internal fun nextPosition(p: Point, currentPoints: HashSet<Point>, directions: List<Direction>): Point {
    val neighbors = p.getNeighbors(currentPoints)
    if (neighbors.isNotEmpty()) {
        directions.find { p.count(it, neighbors) == 0 }?.let { return p + it.point }
    }
    return p
}

internal fun parse(filename: String) =
    ResourceReader.readLines("day23/$filename").toList().mapIndexed { y, line ->
        line.trim().mapIndexed { x, c -> if (c == '#') Point(x, y) else null }.filterNotNull()
    }.flatten().toHashSet()

fun part2(filename: String) {
    var currentPoints = parse(filename)
    var done = false
    var n = 0

    while(!done){
        val out = doRound(n++, currentPoints)
        currentPoints = out.first
        done = !out.second
    }

    println("Done after $n")
}