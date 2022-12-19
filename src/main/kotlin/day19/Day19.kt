package day19

import ResourceReader
import product
import java.util.Deque
import java.util.LinkedList
import java.util.stream.Stream

fun main() {
    part1("example-blueprints.txt")
    part1("my-blueprints.txt")
    part2("example-blueprints.txt")
    part2("my-blueprints.txt")
}

fun part1(filename: String) {
    val summedQualityLevel = getBlueprintStream(filename)
        .map { getMaxGeodes(it) }
        .toList().withIndex().fold(0) { acc, (idx, geodes) ->
            println("Blueprint ${idx + 1} found $geodes geodes")
            acc + (idx + 1) * geodes
        }
    println("For $filename the summed quality level is '$summedQualityLevel'")
}

private fun getBlueprintStream(filename: String): Stream<Cost> =
    ResourceReader.readLines("day19/$filename").map {
        Regex(
            "Blueprint (\\d+): " +
                    "Each ore robot costs (\\d+) ore\\. " +
                    "Each clay robot costs (\\d+) ore\\. " +
                    "Each obsidian robot costs (\\d+) ore and (\\d+) clay\\. " +
                    "Each geode robot costs (\\d+) ore and (\\d+) obsidian\\."
        ).matchEntire(it)!!.groupValues.drop(2)
            .map(String::toShort)
            .let { (oreOre, clayOre, obsOre, obsClay, geoOre, geoObs) ->
                Cost(
                    oreRobot = newMaterials(oreOre, 0, 0, 0),
                    clayRobot = newMaterials(clayOre, 0, 0, 0),
                    obsidianRobot = newMaterials(obsOre, obsClay, 0, 0),
                    geodeRobot = newMaterials(geoOre, 0, geoObs, 0)
                )
            }
    }

private operator fun <T> List<T>.component6() = get(5)

internal typealias State = Array<ShortArray>
internal typealias Materials = ShortArray

internal data class Cost(
    val oreRobot: Materials,
    val clayRobot: Materials,
    val obsidianRobot: Materials,
    val geodeRobot: Materials,
)

internal enum class ResourceType { ORE, CLAY, OBSIDIAN, GEODE }

inline val State.inventory get() = this[0]
inline val State.robots get() = this[1]
inline val Materials.ore get() = this[0]
inline val Materials.clay get() = this[1]
inline val Materials.obsidian get() = this[2]
inline val Materials.geode get() = this[3]

fun newMaterials(ore: Short, clay: Short, obsidian: Short, geode: Short) = shortArrayOf(ore, clay, obsidian, geode)
fun newMaterialsFromInt(ore: Int, clay: Int, obsidian: Int, geode: Int) =
    shortArrayOf(ore.toShort(), clay.toShort(), obsidian.toShort(), geode.toShort())

operator fun Materials.plus(other: Materials): Materials = newMaterialsFromInt(
    this.ore + other.ore,
    this.clay + other.clay,
    this.obsidian + other.obsidian,
    this.geode + other.geode
) // Could also have used this.zip(other).map { (a, b) -> a+b }.toShortArray()

operator fun Materials.minus(other: Materials): Materials = newMaterialsFromInt(
    this.ore - other.ore,
    this.clay - other.clay,
    this.obsidian - other.obsidian,
    this.geode - other.geode
)

fun newStateWithOneOreRobot() = arrayOf(newMaterials(0, 0, 0, 0), newMaterials(1, 0, 0, 0))
fun State.gatherMaterials(): State = arrayOf(inventory + robots, robots)

internal fun State.canAffordRobot(type: ResourceType, cost: Cost) = when (type) {
    ResourceType.ORE      -> inventory.ore >= cost.oreRobot.ore
    ResourceType.CLAY     -> inventory.ore >= cost.clayRobot.ore
    ResourceType.OBSIDIAN -> inventory.ore >= cost.obsidianRobot.ore && inventory.clay >= cost.obsidianRobot.clay
    ResourceType.GEODE    -> inventory.ore >= cost.geodeRobot.ore && inventory.obsidian >= cost.geodeRobot.obsidian
}

internal fun State.constructRobotAndGatherMaterials(type: ResourceType, cost: Cost): State {
    val materialCosts = when (type) {
        ResourceType.ORE      -> cost.oreRobot
        ResourceType.CLAY     -> cost.clayRobot
        ResourceType.OBSIDIAN -> cost.obsidianRobot
        ResourceType.GEODE    -> cost.geodeRobot
    }
    val robotIncrease = newMaterials(0, 0, 0, 0).also { it[type.ordinal] = 1 }
    return arrayOf(inventory - materialCosts + robots, robots + robotIncrease)
}

// Compare pairs by first comparing the second components (the robots) and only if those equal the first (the resources)
val pairComparator = Comparator<Pair<Short, Short>> { (a1, a2), (b1, b2) ->
    when {
        a2 != b2 -> a2.compareTo(b2)
        else     -> a1.compareTo(b1)
    }
}
val stateComparator = Comparator<State> { state1, state2 ->
    // Create list of pairs [(numResources, numRobotForResource)]
    val resourceRobotPairs1: List<Pair<Short, Short>> = state1.inventory.zip(state1.robots)
    val resourceRobotPairs2: List<Pair<Short, Short>> = state2.inventory.zip(state2.robots)
    // we compare the states pairwise starting at geodes and then working back to ore
    resourceRobotPairs1.zip(resourceRobotPairs2)
        .map { (p1, p2) -> pairComparator.compare(p1, p2) }.findLast { it != 0 } ?: 0
}

internal fun getMaxGeodes(costs: Cost, maxTime: Int = 24, pruneAmount: Int = 10_000): Short {
    val maxCosts = costs.maxCosts()

    // BFS with branch pruning using above comparator
    var currentStates: Deque<State> = LinkedList(listOf(newStateWithOneOreRobot()))
    repeat(maxTime) {
        val newStates = LinkedList<State>()
        while (currentStates.isNotEmpty()) {
            val state = currentStates.pop()

            // save for the future
            newStates.add(state.gatherMaterials())

            // spend it all on new shiny robots
            ResourceType.values().forEach { type ->
                if (state.canAffordRobot(type, costs) && state.shouldStillBuy(type, maxCosts)) {
                    newStates.add(state.constructRobotAndGatherMaterials(type, costs))
                }
            }
        }
        currentStates = LinkedList(newStates.sortedWith(stateComparator.reversed()).take(pruneAmount))  // pruning
    }
    return currentStates.maxOf { it.inventory.geode }
}

internal fun Cost.maxOre() = listOf(oreRobot, clayRobot, obsidianRobot, geodeRobot).maxBy { it.ore }.ore
internal fun Cost.maxCosts() = newMaterials(this.maxOre(), obsidianRobot.clay, geodeRobot.obsidian, Short.MAX_VALUE)

internal fun State.shouldStillBuy(type: ResourceType, maxCosts: Materials) = when (type) {
    ResourceType.ORE      -> robots.ore < maxCosts.ore
    ResourceType.CLAY     -> robots.clay < maxCosts.clay
    ResourceType.OBSIDIAN -> robots.obsidian < maxCosts.obsidian
    ResourceType.GEODE    -> true
}

fun part2(filename: String, minutes: Int = 32) {
    val qualityProduct = getBlueprintStream(filename).toList().take(3)
        .mapIndexed { idx, cost ->
            val geodes = getMaxGeodes(cost, minutes)
            println("Blueprint ${idx + 1} found $geodes geodes")
            geodes.toInt()
        }.product()
    println("For $filename the product of the quality levels after $minutes is '$qualityProduct'")
}
