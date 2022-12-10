package day09

import ResourceReader
import kotlin.math.abs

fun main() {
    moveRope("example-input.txt", true)
    moveRope("my-input.txt")

    println("-----------------")
    moveRope("example-input.txt", 2, true)
    moveRope("example-input2.txt", 10, false)
    moveRope("my-input.txt", 10, false)
}

object MoveInputReader {
    fun read(filename: String) = ResourceReader.readLines("day9/$filename").toList()
        .flatMap { cmd -> List(cmd.substring(2).toInt()) { MoveDirection.valueOf("${cmd[0]}") } }
}

enum class MoveDirection { U, D, L, R }

typealias Coord = Pair<Int, Int>

val Coord.x get() = first
val Coord.y get() = second
operator fun Coord.minus(other: Coord) = (this.x - other.x) to (this.y - other.y)

val ORIGIN = 0 to 0

/* ====================== Solution part 1 ====================== */

fun moveRope(movesFilename: String, drawOutput: Boolean = false): Set<Coord> {
    var head = ORIGIN
    var tail = ORIGIN
    val placesTailVisited = mutableSetOf<Coord>()

    val moves = MoveInputReader.read(movesFilename)
    for ((turn, move) in moves.withIndex()) {
        head = head.move(move)
        tail = newKnotPosition(head, tail)

        placesTailVisited.add(tail)

        if (drawOutput) {
            println("=== Turn $turn : $move ===")
            drawPositions(head, tail)
        }
    }
    println("Given '$movesFilename' the tail visited '${placesTailVisited.size}' places: $placesTailVisited")
    return placesTailVisited
}

fun Coord.move(moveDirection: MoveDirection) = when (moveDirection) {
    MoveDirection.U -> x to (y + 1)
    MoveDirection.D -> x to (y - 1)
    MoveDirection.L -> (x - 1) to y
    MoveDirection.R -> (x + 1) to y
}

fun sgn(x: Int) = if (x >= 0) +1 else -1

private fun newKnotPosition(head: Coord, tail: Coord): Coord {
    val (dx, dy) = head - tail
    return if ((abs(dx) > 1 && abs(dy) >= 1) || (abs(dx) >= 1 && abs(dy) > 1)) {
        (tail.x + sgn(dx)) to (tail.y + sgn(dy))
    } else if (abs(dx) > 1) {
        (tail.x + sgn(dx)) to tail.y
    } else if (abs(dy) > 1) {
        tail.x to (tail.y + sgn(dy))
    } else {
        tail.x to tail.y
    }
}

fun drawPositions(head: Coord, tail: Coord) {
    val (minX, maxX, minY, maxY) = listOf(0, 6, 0, 5)
    (maxY - 1 downTo minY).map { y ->
        (minX until maxX).map { x ->
            when {
                (x to y) == head -> print("H")
                (x to y) == tail -> print("T")
                x == 0 && y == 0 -> print("s")
                else             -> print(".")
            }
        }
        println()
    }
}


/* ====================== Solution part 2 ====================== */

fun <T> List<T>.replaceElementAt(idx: Int, newValue: T) = subList(0, idx) + newValue + subList(idx + 1, size)

fun moveRope(movesFilename: String, numberOfKnots: Int = 10, drawOutput: Boolean = false): Set<Coord> {
    var knots = List(numberOfKnots) { ORIGIN }
    val placesTailVisited = mutableSetOf<Coord>()
    val moves = MoveInputReader.read(movesFilename)

    for ((turn, moveDirection) in moves.withIndex()) {
        // move the head
        knots = knots.replaceElementAt(0, knots.first().move(moveDirection))

        // then move all subsequent knots
        for (i in 1 until knots.size) {
            knots = knots.replaceElementAt(i, newKnotPosition(knots[i - 1], knots[i]))
        }

        placesTailVisited.add(knots.last())

        if (drawOutput) {
            println("=== Turn $turn : $moveDirection ===")
            drawPositions(knots)
        }

    }
    println("Given '$movesFilename' the tail visited '${placesTailVisited.size}' places: $placesTailVisited")
    return placesTailVisited
}

fun drawPositions(knots: List<Coord>) {
    val (minX, maxX, minY, maxY) = listOf(-11, 15, -5, 16)
    (maxY - 1 downTo minY).map { y ->
        (minX until maxX).map { x ->
            when {
                (x to y) == knots[0] -> print("H")
                (x to y) in knots    -> print(knots.withIndex().find { it.value == x to y }!!.index)
                (x to y) == ORIGIN   -> print("s")
                else                 -> print(".")
            }
        }
        println()
    }
}