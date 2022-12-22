@file:OptIn(ExperimentalStdlibApi::class)

package day22

import ResourceReader
import day22.Direction.*

fun main() {
    part1("example-directions.txt")
    part1("my-directions.txt")
//    part2("example-.txt")  // skipping as folding schema is different from my input
    part2("my-directions.txt")
}

enum class Direction(val x: Int, val y: Int) { RIGHT(+1, 0), DOWN(0, +1), LEFT(-1, 0), UP(0, -1); }

fun Direction.rotate(amount: Int) = Direction.values()[Math.floorMod(this.ordinal + amount, 4)]

operator fun List<CharArray>.get(y: Int, x: Int) = this[Math.floorMod(y, this.size)][Math.floorMod(x, this[0].size)]

fun part1(filename: String) {
    val (mapStr, instructions) = ResourceReader.readString("day22/$filename").split(Regex("\\n\\s?\\n"))
    val (height, width) = mapStr.lines().size - 1 to mapStr.lines().maxOf { it.length }
    val map = mapStr.lines().map { it.toCharArray() }.dropLast(1).map { it + CharArray(width - it.size) { ' ' } }

    var (x, y) = map[0].withIndex().find { it.value == '.' }!!.index to 0
    var direction = RIGHT

    fun update(newX: Int, newY: Int) {
        x = Math.floorMod(newX, width)
        y = Math.floorMod(newY, height)
    }

    fun nextLocation(x: Int, y: Int, dir: Direction): Pair<Int, Int> {
        var n = 1
        while (map[y + dir.y * n, x + dir.x * n] == ' ') {
            n++
        }
        return x + dir.x * n to y + dir.y * n
    }

    fun move(amount: Int) {
        var stepsMade = 0
        while (stepsMade < amount) {
            val (nextX, nextY) = nextLocation(x, y, direction)
            when (map[nextY, nextX]) {
                '.'  -> update(nextX, nextY)
                '#'  -> return
                else -> throw IllegalStateException()
            }
            stepsMade++
        }
    }

    Regex("\\d+|[RL]").findAll(instructions).map { it.value }.forEach {
        when (it) {
            "R"  -> direction = direction.rotate(+1)
            "L"  -> direction = direction.rotate(-1)
            else -> move(it.toInt())
        }
//            println("Got input '$it', moved to (x,y)=($x,$y) and direction $direction")
    }
    println("Password for $filename is '${1000 * (y + 1) + 4 * (x + 1) + direction.ordinal}'")
}


fun part2(filename: String) {
    val (mapStr, instructions) = ResourceReader.readString("day22/$filename").split(Regex("\\n\\s?\\n"))
    val (height, width) = 200 to 150
    val map = mapStr.lines().map { it.toCharArray() }.dropLast(1).map { it + CharArray(width - it.size) { ' ' } }

    var (x, y) = map[0].withIndex().find { it.value == '.' }!!.index to 0
    var direction = RIGHT

    fun update(newX: Int, newY: Int, newDirection: Direction) {
        x = Math.floorMod(newX, width)
        y = Math.floorMod(newY, height)
        direction = newDirection
    }

    fun move(amount: Int) {
        var stepsMade = 0
        while (stepsMade < amount) {
            val (nextX, nextY, nextDirection) = nextLocationAndDirection(x, y, direction)
            when (map[nextY, nextX]) {
                '.'  -> update(nextX, nextY, nextDirection)
                '#'  -> return
                else -> throw IllegalStateException()
            }
            stepsMade++
        }
    }

    Regex("\\d+|[RL]").findAll(instructions).map { it.value }.forEach {
        when (it) {
            "R"  -> direction = direction.rotate(+1)
            "L"  -> direction = direction.rotate(-1)
            else -> move(it.toInt())
        }
//        println("Got input '$it', moved to (x,y)=($x,$y) and direction $direction")
    }
    println("Password for $filename with cubic folding is '${1000 * (y + 1) + 4 * (x + 1) + direction.ordinal}'")
}

/*
The pasting schema for my cube

    0     50   100   149
  0       +---a-+---b-+
          f     |     |
          |     |     c
 50       +-----+---d-+
          |     |
          g     d
100 +---g-+-----+
    |     |     c
    f     |     |
150 +-----+-e---+
    |     e
    a     |
199 +---b-+

 */

fun nextLocationAndDirection(x: Int, y: Int, dir: Direction): Triple<Int, Int, Direction> =
    when {
        y == 0 && x in 50..<100 && dir == UP      -> Triple(0, 100 + x, RIGHT) // a
        y == 0 && x in 100..<150 && dir == UP     -> Triple(x - 100, 199, UP) // b
        x == 149 && y in 0..<50 && dir == RIGHT   -> Triple(99, 149 - y, LEFT) // c
        y == 49 && x in 100..<150 && dir == DOWN  -> Triple(99, x - 50, LEFT) // d
        x == 99 && y in 50..<100 && dir == RIGHT  -> Triple(y + 50, 49, UP) // d
        x == 99 && y in 100..<150 && dir == RIGHT -> Triple(149, 149 - y, LEFT)// c
        y == 149 && x in 50..<100 && dir == DOWN  -> Triple(49, 100 + x, LEFT) // e
        x == 49 && y in 150..<200 && dir == RIGHT -> Triple(y - 100, 149, UP) // e
        y == 199 && x in 0..<50 && dir == DOWN    -> Triple(100 + x, 0, DOWN) // b
        x == 0 && y in 150..<200 && dir == LEFT   -> Triple(y - 100, 0, DOWN) // a
        x == 0 && y in 100..<150 && dir == LEFT   -> Triple(50, 149 - y, RIGHT) // f
        y == 100 && x in 0..<50 && dir == UP      -> Triple(50, 50 + x, RIGHT) // g
        x == 50 && y in 50..<100 && dir == LEFT   -> Triple(y - 50, 100, DOWN) // g
        x == 50 && y in 0..<50 && dir == LEFT     -> Triple(0, 149 - y, RIGHT) // f
        else                                      -> Triple(x + dir.x, y + dir.y, dir)
    }

