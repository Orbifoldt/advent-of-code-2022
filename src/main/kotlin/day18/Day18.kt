package day18

import ResourceReader
import java.util.Deque
import java.util.LinkedList
import kotlin.math.abs

fun main() {
    part1("example-lava.txt")
    part1("my-lava.txt")
    part2("example-lava.txt")
    part2("my-lava.txt")
}

fun part1(filename: String) {
    val cubes = getCubes(filename)
    println("Total surface area of lava from $filename is '${cubes.sumOf { it.uncoveredSurface(cubes) }}'")
}

private fun getCubes(filename: String): MutableList<Cube> = ResourceReader.readLines("day18/$filename").map {
    it.trim().split(",").map(String::toInt).let { (x, y, z) -> Cube(x, y, z) }
}.toList()


data class Cube(val x: Int, val y: Int, val z: Int)

fun Cube.l1DistanceTo(other: Cube) = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

fun Cube.uncoveredSurface(otherCubes: List<Cube>) = 6 - otherCubes.count { this.l1DistanceTo(it) == 1 }


fun part2(filename: String) {
    val cubes = getCubes(filename)
    val exteriorSurface = cubes.findExternalSurfaceArea()
    println("Total exterior surface area of lava from $filename is '$exteriorSurface'")
}

fun Cube.neighbors() = listOf(
    copy(x = x + 1), copy(x = x - 1),
    copy(y = y - 1), copy(y = y + 1),
    copy(z = z - 1), copy(z = z + 1),
)

fun List<Cube>.findExternalSurfaceArea(): Int {
    val boundingBoxSizes = Cube(maxBy { abs(it.x) }.x + 1, maxBy { abs(it.y) }.y + 1, maxBy { abs(it.z) }.z + 1)

    val isExternal = (-boundingBoxSizes.x..boundingBoxSizes.x).flatMap { x ->
        (-boundingBoxSizes.y..boundingBoxSizes.y).flatMap { y ->
            (-boundingBoxSizes.z..boundingBoxSizes.z).map { z ->
                Cube(x, y, z) to false
            }
        }
    }.toMap().toMutableMap()

    val cubesToCheck: Deque<Cube> = LinkedList()
    cubesToCheck.add(boundingBoxSizes)

    while (cubesToCheck.isNotEmpty()) {
        val current = cubesToCheck.pop()
        isExternal[current] = true
        current.neighbors().forEach { if (it !in this && isExternal[it] == false) cubesToCheck.push(it) }
    }

    return this.sumOf { cube ->
        cube.neighbors().sumOf { neighbor ->
            (if (isExternal[neighbor]!!) 1 else 0) as Int
        }
    }
}
