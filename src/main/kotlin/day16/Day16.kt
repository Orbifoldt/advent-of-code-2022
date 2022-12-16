@file:OptIn(ExperimentalStdlibApi::class)

package day16

import ResourceReader
import times

fun main() {
    findMaxFlowWalk("example-pipes.txt")
    findMaxFlowWalk("my-pipes.txt")
    findMaxFlowWalkWithTwoPlayers("example-pipes.txt")
    findMaxFlowWalkWithTwoPlayers("my-pipes.txt")
}

fun findMaxFlowWalk(filename: String, maxTime: Int = 30): Walk {
    val valveGraph = createValveGraph(filename)
    val start = valveGraph.first { it.name == "AA" }

    // We use iterative DFS:
    var walkStack = listOf(Walk(listOf(start)))

    for (currentTime in 1..<maxTime) {
        val continuedWalks = mutableListOf<Walk>()
        walkStack.forEach { walk ->
            val currentValve = walk.currentValve()

            if (currentValve !in walk.opened && currentValve.flowRate > 0) {
                continuedWalks += walk.stayAndOpenCurrentValve()
            }

            currentValve.connections.map(walk::moveToNext)
                .let(continuedWalks::addAll)
        }

        // For each tick we trim the walks considered to only contain the best so far (found by trial and error)
        walkStack = continuedWalks.sortedDescending().take(20_000)
    }

    return walkStack.first().also { println("In $filename the maximal flow we can achieve is '${it.totalFlow()}'.") }
}

fun findMaxFlowWalkWithTwoPlayers(filename: String, maxTime: Int = 26): DuoWalk {
    val valveGraph = createValveGraph(filename)
    val start = valveGraph.first { it.name == "AA" }
    val maximumOpenableValves = valveGraph.count { it.flowRate > 0 }

    // We use iterative DFS:
    var walkStack = listOf(DuoWalk(Walk(listOf(start)), Walk(listOf(start))))

    for (currentTime in 1..<maxTime) {
        val continuedWalks = mutableListOf<DuoWalk>()

        for ((myWalk, elephantsWalk) in walkStack) {
            // With two walkers we actually quickly open all valves, so then we can stop
            if (myWalk.opened.count() + elephantsWalk.opened.count() >= maximumOpenableValves) {
                continue
            }

            val myValve = myWalk.currentValve()
            val myNextWalks = mutableListOf<Walk>()
            // Stay and open the current valve:
            if (myValve !in myWalk.opened && myValve !in elephantsWalk.opened && myValve.flowRate > 0) {
                myNextWalks += myWalk.stayAndOpenCurrentValve()
            }
            // or, move to some connecting valve:
            myValve.connections.map(myWalk::moveToNext)
                .let(myNextWalks::addAll)

            val elephantsValve = elephantsWalk.currentValve()
            val elephantNextWalks = mutableListOf<Walk>()
            // Stay and open the current valve:
            if (elephantsValve !in myWalk.opened
                && elephantsValve !in elephantsWalk.opened
                && elephantsValve.flowRate > 0
                && elephantsValve != myValve  // We can't both turn the same valve at the same time
            ) {
                elephantNextWalks += elephantsWalk.stayAndOpenCurrentValve()
            }
            // or, move to some connecting valve:
            elephantsValve.connections.map(elephantsWalk::moveToNext)
                .let(elephantNextWalks::addAll)

            (myNextWalks * elephantNextWalks)
                .map { (a, b) -> DuoWalk(a, b) }
                .let(continuedWalks::addAll)
        }

        // For each tick we trim the walks considered to only contain the best so far (found by trial and error)
        walkStack = continuedWalks.sortedDescending().take(20_000)
    }

    return walkStack.first()
        .also { println("In $filename the maximal flow me and the elephant can achieve is '${it.totalFlow()}'.") }
}


fun createValveGraph(filename: String): List<Valve> = ResourceReader.readLines("day16/$filename")
    .map {
        "Valve (\\w{2}) has flow rate=(\\d+); tunnels? leads? to valves? (.*)".toRegex()
            .matchEntire(it.trim())!!.groupValues.let { (_, name, rate, connections) ->
                Valve(name, rate.toInt(), emptyList()) to connections.replace(" ", "").split(",")
            }
    }.toList().let { graphData ->
        val allValves = graphData.map { it.first }
        graphData.forEach { (valve, connections) ->
            connections.forEach { valve.connections += allValves.first { aValve -> aValve.name == it } }
        }
        allValves
    }

data class Valve(val name: String, val flowRate: Int, var connections: List<Valve>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Valve
        return name == other.name
    }

    override fun hashCode() = name.hashCode()
    override fun toString() = "Valve(name='$name', flowRate=$flowRate, connections=${connections.map { it.name }})"
}

data class Walk(val valves: List<Valve>, val opened: Map<Valve, Int> = emptyMap(), val time: Int = 1) :
    Comparable<Walk> {
    fun currentValve(): Valve = valves.last()
    fun stayAndOpenCurrentValve(): Walk = Walk(valves + currentValve(), opened + (currentValve() to time), time + 1)
    fun moveToNext(valve: Valve): Walk = Walk(valves + valve, opened, time + 1)
    fun totalFlow(): Int = opened.map { (valve, t) -> valve.flowRate * (time - t) }.sum()
    override fun compareTo(other: Walk): Int = totalFlow().compareTo(other.totalFlow())
}

data class DuoWalk(val myWalk: Walk, val elephantsWalk: Walk) : Comparable<DuoWalk> {
    fun totalFlow() = myWalk.totalFlow() + elephantsWalk.totalFlow()
    override fun compareTo(other: DuoWalk) = totalFlow().compareTo(other.totalFlow())
}
