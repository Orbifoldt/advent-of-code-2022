@file:OptIn(ExperimentalStdlibApi::class)

package day17

import ResourceReader
import loopingSequence

internal typealias Coord = Pair<Int, Int>

fun main() {
    part1("example-gas-jets.txt")
    part1("my-gas-jets.txt")
    part2("example-gas-jets.txt")
    part2("my-gas-jets.txt")
}

fun part1(filename: String, numRocks: Int = 2022): Board =
    dropRocksUntil(filename, verbose = true) { board: Board -> board.rocksDropped == numRocks }

class Board(val width: Int = 7) {
    private val state = MutableList(4) { emptyRow() }
    private var _rocksDropped = 0

    val towerHeight get() = state.withIndex().find { row -> row.value.none { it } }?.index ?: 0
    val rocksDropped get() = _rocksDropped

    operator fun get(y: Int, x: Int) = if (y < state.size) state[y][x] else false
    operator fun get(y: Int) = state[y].clone()

    fun canRockBeAt(coord: Coord, rock: Rock): Boolean {
        val (a, b) = coord
        for ((x, y) in rock.coords) {
            when {
                (x + a) !in 0..<width -> return false
                (y + b) < 0           -> return false
                this[y + b, x + a]    -> return false
            }
        }
        return true
    }

    fun placeRock(coord: Coord, rock: Rock) {
        // Increase board size to accommodate the next rock:
        while (state.size <= coord.second + rock.height) {
            state.add(emptyRow())
        }

        for ((x, y) in rock.coords) {
            state[coord.second + y][coord.first + x] = true
        }
        _rocksDropped++
    }

    private fun emptyRow() = BooleanArray(width)

    override fun toString(): String = buildString {
        state.reversed().forEach { row ->
            append("|")
            row.forEach { pixel -> append(if (pixel) "#" else ".") }
            appendLine("|")
        }
        appendLine("+-------+")
    }
}

private fun dropRocksUntil(filename: String, verbose: Boolean = false, stopCondition: (Board) -> Boolean): Board {
    val board = Board(width = 7)
    val jetSource = ResourceReader.readString("day17/$filename").trim().toCharArray()
        .map { if (it == '<') -1 else +1 }.loopingSequence().iterator()
    val fallingRockSource = Rock.values().toList().loopingSequence().iterator()
    val (spawnX, spawnY) = 2 to 4

    while (!stopCondition(board)) {
        val nextRock = fallingRockSource.next()
        var (x, y) = spawnX to board.towerHeight + spawnY

        while (true) {
            if (board.canRockBeAt(x to (y - 1), nextRock)) {
                y--
            } else {
                break  // The rock settles
            }

            val nextJet = jetSource.next()
            if (board.canRockBeAt((x + nextJet) to y, nextRock)) {
                x += nextJet
            }
        }
        board.placeRock(x to y, nextRock)
        if (verbose && board.rocksDropped % 1000 == 0) println("Dropped ${board.rocksDropped} rocks...")
    }
    if (verbose) println("For jets $filename after dropping ${board.rocksDropped} the tower has a height of '${board.towerHeight}'")
    return board
}


fun part2(filename: String, totalRocks: Long = 1_000_000_000_000) {
    // Because of the repeating inputs (both rocks and jets) and the finite width of the board eventually the rock
    // tower will start to repeat. This, however, doesn't mean that the repetitions start at y=0, this may only start
    // occurring higher up.

    // First we find the height of a single period (a repetition)
    val initialRocks = 10_000
    val board = dropRocksUntil(filename) { it.rocksDropped == initialRocks }
    val startY = 2000  // far enough away from the bottom which might not be part of the repetitions
    val periodHeight = board.findPeriod(startY)
    if (periodHeight != null) {
        println("Period found! $periodHeight")
    } else {
        println("No period found")
        return
    }

    // Knowing the period's height we can now determine the number of rocks that go into one period
    val boardAtPeriodStart = dropRocksUntil(filename) { it.towerHeight >= startY }
    val periodStartRockCount = boardAtPeriodStart.rocksDropped
    val periodEndRockCount = dropRocksUntil(filename) { it.towerHeight >= startY + periodHeight }.rocksDropped
    val rocksInPeriod = periodEndRockCount - periodStartRockCount

    // There likely won't exactly fit an integer number of periods into our desired number of rocks, so we find the rest
    val restRocks = totalRocks % rocksInPeriod

    // Also, the start is not part of the repeating periods, so we need to know how many periods worth of rocks we need
    // to have until it does start repeating
    val minStartingPeriods = (periodStartRockCount / rocksInPeriod + 1)
    assert(minStartingPeriods * periodHeight  > startY)

    // Now we know the minimal starting amount of rocks after which we can start counting the periods
    val minStartingRocks = restRocks.toInt() + minStartingPeriods * rocksInPeriod
    val minStartingHeight = dropRocksUntil(filename) {it.rocksDropped == minStartingRocks}.towerHeight
    assert((totalRocks - minStartingRocks) % rocksInPeriod == 0L)

    // After this minimal starting amount of rocks we simply count how many periods are left and then find the height
    val remainingPeriods = (totalRocks - minStartingRocks) / rocksInPeriod
    val totalHeight = minStartingHeight + remainingPeriods * periodHeight
    println("After $totalRocks the tower has a height of $totalHeight")
}

fun Board.findPeriod(startY: Int): Int? {
    val rowToCompare = this[startY]
    var y: Int = startY + 10 // in case it's just a few lines repeating
    while (y < towerHeight) {
        y = findIndexOfRowEqualTo(rowToCompare, startRowIndex = y) ?: return null

        val candidatePeriod = y - startY
        if (isValidPeriod(startY, candidatePeriod)) {
            return candidatePeriod
        } else y++
    }
    return null
}

fun Board.findIndexOfRowEqualTo(rowToCompare: BooleanArray, startRowIndex: Int): Int? {
    for (y in startRowIndex..<towerHeight) {
        if (this[y].contentEquals(rowToCompare)) return y
    }
    return null
}

fun Board.isValidPeriod(startY: Int, period: Int): Boolean {
    for (y in startY..<startY + period) {
        if (y + period > towerHeight) return false
        if (!this[y].contentEquals(this[y + period])) {
            return false
        }
    }
    return true
}
