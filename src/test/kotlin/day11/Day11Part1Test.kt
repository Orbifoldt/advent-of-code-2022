package day11

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.LinkedList

internal class Day11Part1Test {
    private val monkeys = readMonkeysFrom("example-strategy.txt")

    private fun List<Monkey>.gatherItemsAsLongs() = map { monkey -> monkey.startingItems.toList().map { it.toLong() } }

    @Test
    fun `should be able to create a monkeys from file input`() {
        assertThat(monkeys.first()).isEqualTo(Monkey(
            name = 0,
            startingItems = LinkedList(listOf(79, 98)),
            operation = "old * 19",
            test = 23L,
            ifTrue = 2,
            ifFalse = 3,
        ))
        monkeys.withIndex().forEach { (idx, monkey) -> assertThat(monkey.name).isEqualTo(idx) }
    }

    @Test
    fun `After a single round the monkeys should hold the correct items`() {
        val items = monkeys.doRound(verbose = true).gatherItemsAsLongs()
        assertThat(items).containsExactly(
            listOf(20, 23, 27, 26),
            listOf(2080, 25, 167, 207, 401, 1046),
            listOf(),
            listOf(),
        )
    }

    @Test
    fun `After ten rounds the monkeys should hold the correct items`() {
        repeat(10) { monkeys.doRound() }

        val items = monkeys.gatherItemsAsLongs()
        assertThat(items).containsExactly(
            listOf(91, 16, 20, 98),
            listOf(481, 245, 22, 26, 1092, 30),
            listOf(),
            listOf(),
        )
    }


    @Test
    fun `After 20 rounds the monkeys should hold the correct items`() {
        repeat(20) { monkeys.doRound() }

        val items = monkeys.gatherItemsAsLongs()
        assertThat(items).containsExactly(
            listOf(10, 12, 14, 26, 34),
            listOf(245, 93, 53, 199, 115),
            listOf(),
            listOf(),
        )
    }


    @Test
    fun `After 20 rounds the monkeys should have inspected the expected number of items`() {
        repeat(20) { monkeys.doRound() }

        assertThat(monkeys[0].numberOfInspects).isEqualTo(101)
        assertThat(monkeys[1].numberOfInspects).isEqualTo(95)
        assertThat(monkeys[2].numberOfInspects).isEqualTo(7)
        assertThat(monkeys[3].numberOfInspects).isEqualTo(105)
    }

    @Test
    fun `After 20 rounds the monkey business level should equal 10605`() {
        assertThat(determineMonkeyBusinessLevel("example-strategy.txt", 20))
            .isEqualTo(10605L)
    }


    @Test
    fun `After 20 rounds without worry decrease the monkeys should have inspected the expected number of items`() {
        repeat(20) { monkeys.doRound(worryQuotient = 1) }
        assertThatMonkeysHaveInspectedItems(99, 97, 8, 103)
    }

    @Test
    @Disabled("Using long causes overflow, using BigInt will take too long")
    fun `After 1000 rounds without worry decrease the monkeys should have inspected the expected number of items`() {
        repeat(1000) { monkeys.doRound(worryQuotient = 1) }
        assertThatMonkeysHaveInspectedItems(5204, 4792, 199, 5192)
    }

    private fun assertThatMonkeysHaveInspectedItems(vararg numberOfInspects: Int) {
        assertThat(numberOfInspects.size)
            .withFailMessage("Should provide as many varargs as monkeys, expected '${monkeys.size}'.")
            .isEqualTo(monkeys.size)

        numberOfInspects.withIndex().forEach {
            assertThat(monkeys[it.index].numberOfInspects)
                .withFailMessage(
                    "Expected monkey ${it.index} to have inspected '${it.value}' times, " +
                            "but actual was '${monkeys[it.index].numberOfInspects}'"
                )
                .isEqualTo(it.value)
        }
    }
}