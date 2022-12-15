package day15

import kotlin.math.abs

fun main() {
    part1("example-sensors.txt", 10)
    part1("my-sensors.txt", 2_000_000)
    part2("example-sensors.txt", 20)
    part2("my-sensors.txt", 4_000_000)
}

internal typealias Coord = Pair<Int, Int>

internal inline val Coord.x get() = first
internal inline val Coord.y get() = second

fun part1(filename: String, y: Int) = getSensorsAndBeacons(filename).calculateNonBeacons(y)

private fun getSensorsAndBeacons(filename: String) = ResourceReader.readLines("day15/$filename").toList().map {
        "Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)\\s*".toRegex()
            .matchEntire(it)!!.groupValues.drop(1).map(String::toInt)
    }.map { (sx, sy, bx, by) -> (sx to sy) to (bx to by) }

private fun List<Pair<Coord, Coord>>.calculateNonBeacons(y: Int) =
    println("At y=$y there can't be beacons at ${this.getCoveredXs(y).count()} places")

private fun List<Pair<Coord, Coord>>.getCoveredXs(y: Int): List<Int> {
    val covered = this.map { (s, b) ->
        val d = dist(s, b) - abs(s.y - y)
        (s.x - d)..(s.x + d)  // if b > a then a..b is just an empty range
    }.fold(emptySet()) { acc: Set<Int>, intRange -> acc.union(intRange) }.toList()

    val beaconsOnThisRow = this.filter { (_, b) -> b.y == y }.map { (_, b) -> b.x }
    return covered.filterNot { it in beaconsOnThisRow }
}

fun dist(a: Coord, b: Coord) = abs(a.x - b.x) + abs(a.y - b.y)

@Suppress("ReplaceRangeToWithUntil")
fun part2(filename: String, max: Int) {
    val (sensors, beacons) = getSensorsAndBeacons(filename).run { map { it.first } to map { it.second } }
    val sensorsAndBeaconDistances = sensors.zip(beacons).map { (s, b) -> s to dist(s, b) }

    val theBeacon = sensorsAndBeaconDistances.asSequence()  // Sequence so that we don't have it all in memory
        .flatMap { (s, d) ->  // Generate the edges just outside the diamond
            (s.x - d - 1..s.x - 1).zip(s.y..s.y + d) +                     /* W to N */
                    (s.x..s.x + d).zip(s.y + d + 1 downTo s.y + 1) +       /* N to E */
                    (s.x + d + 1 downTo s.x + 1).zip(s.y downTo s.y - d) + /* E to S */
                    (s.x downTo s.x - d).zip(s.y - d - 1..s.y - 1)         /* S to W */
        }
        .filter { it !in beacons }
        .filter { s -> s.x in 0..max && s.y in 0..max }
        .first { it.isOutsideAllRanges(sensorsAndBeaconDistances) }  // the unique beacon is outside all diamonds
    println("The beacon is at $theBeacon, it has tuning frequency ${theBeacon.x.toLong() * max + theBeacon.y}")
}

fun Coord.isOutsideAllRanges(sensorsAndBeaconDistances: List<Pair<Coord, Int>>): Boolean {
    sensorsAndBeaconDistances.forEach { (s, d) ->
        if (dist(this, s) <= d) return false
    }
    return true
}




