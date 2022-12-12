package day12

import ResourceReader
import kotlin.math.abs
import kotlin.math.min

fun main() {
    part1("example-mountain.txt")
    part1("my-mountain.txt")
    part2("example-mountain.txt")
    part2("my-mountain.txt") // 465
}

fun part1(filename: String) {
    val nodes = generateNodes(filename)
    val unvisited = nodes.flatten().toMutableSet()

    val start = unvisited.find { it.value == 'S'.code }!!.also { it.value = 'a'.code }
    val end = unvisited.find { it.value == 'E'.code }!!.also { it.value = 'z'.code }
    start.dist = 0
    dijkstra(start, unvisited, Node::distanceTo)

    printMountain(nodes)
    println("Shortest path to the end takes ${end.dist} steps!\n")
}

fun part2(filename: String) {
    val nodes = generateNodes(filename)
    val unvisited = nodes.flatten().toMutableSet()

    unvisited.find { it.value == 'S'.code }!!.also { it.value = 'a'.code }
    // We do the inverse: start in E and then look at the distances from any other point to E
    val start = unvisited.find { it.value == 'E'.code }!!.also { it.value = 'z'.code }
    start.dist = 0

    // need to reverse the metric: you can always go from high to low, but from low to high only if difference is 1
    fun Node.inverseDistance(other: Node) = other.distanceTo(this)
    dijkstra(start, unvisited, Node::inverseDistance)

    val min = nodes.flatten().filter { it.value == 'a'.code }.minByOrNull { it.dist }!!
    printMountain(nodes)
    println("Min path length between 'a'-zone and E is ${min.dist}, the location is row=${min.y} and column=${min.x}")
}

private fun generateNodes(filename: String): List<List<Node>> {
    val map = ResourceReader.readLines("day12/$filename").map { it.trim().toCharArray().map(Char::code) }.toList()
    val nodes = map.mapIndexed { y, row ->
        row.mapIndexed { x, c ->
            Node(x, y, c, Int.MAX_VALUE, false)
        }
    }
    return nodes
}

data class Node(val x: Int, val y: Int, var value: Int, var dist: Int, var visited: Boolean) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Node
        return (x == other.x && y == other.y)
    }

    override fun hashCode() = 31 * x + y  // Since we use a LinkedHashSet the hash should not change if we mutate the Node
}

tailrec fun dijkstra(current: Node, unvisited: MutableSet<Node>, metric: Node.(Node) -> Int) {
    unvisited.findUnvisitedNeighbors(current)
        .updateDistanceFrom(current, metric)
    current.visited = true
    unvisited.remove(current)

    val next = unvisited.minByOrNull { it.dist }
    if (next == null) {
        return
    } else {
        return dijkstra(next, unvisited, metric)
    }
}

fun MutableSet<Node>.findUnvisitedNeighbors(current: Node) =
    current.let { (x, y) -> filter { !it.visited && it.isNeighborOf(x, y) } }

fun Node.isNeighborOf(x: Int, y: Int) =
    ((this.x to this.y) in listOf(x to (y + 1), x to (y - 1), (x + 1) to y, (x - 1) to y))

fun List<Node>.updateDistanceFrom(current: Node, metric: Node.(Node) -> Int) = forEach {
    val distance = current.metric(it)
    if (current.dist != Int.MAX_VALUE && distance != Int.MAX_VALUE) {
        it.dist = min(current.dist + distance, it.dist)
    }
}

fun Node.distanceTo(other: Node): Int {
    val l1Dist = abs(this.x - other.x) + abs(this.y - other.y)
    return if ((l1Dist <= 1) && (other.value - this.value <= 1)) l1Dist else Int.MAX_VALUE
}

private fun printMountain(nodes: List<List<Node>>, n: Int = 3) {
    nodes.forEach { row ->
        row.forEach {
            print((if (it.dist == Int.MAX_VALUE) "∞" else it.dist.toString()).padStart(n, ' ') + " ")
        }
        println()
    }
}


