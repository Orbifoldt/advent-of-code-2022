package day11

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import product

internal class Day11Part2Test {
    private val monkeys = readMonkeysFrom("example-strategy.txt")
    private val modulus = monkeys.map { it.test }.product()

    @Test
    fun `After a single round without worry decrease the monkeys should have inspected the expected number of items`() {
        monkeys.doRoundModulusOnly(modulus)
        assertThatMonkeysHaveInspectedItems(2, 4, 3, 6)
    }

    @Test
    fun `After 20 rounds without worry decrease the monkeys should have inspected the expected number of items`() {
        repeat(20) { monkeys.doRoundModulusOnly(modulus) }
        assertThatMonkeysHaveInspectedItems(99, 97, 8, 103)
    }


    @Test
    fun `After 1000 rounds without worry decrease the monkeys should have inspected the expected number of items`() {
        repeat(1000) { monkeys.doRoundModulusOnly(modulus) }
        assertThatMonkeysHaveInspectedItems(5204, 4792, 199, 5192)
    }


    @Test
    fun `After 4000 rounds without worry decrease the monkeys should have inspected the expected number of items`() {
        repeat(4000) { monkeys.doRoundModulusOnly(modulus) }
        assertThatMonkeysHaveInspectedItems(20858, 19138, 780, 20797)
    }

    @Test
    fun `After 10,000 rounds without worry decrease the monkey business level should equal 2713310158`() {
        assertThat(determineMonkeyBusinessLevelModulusOnly("example-strategy.txt", 10_000))
            .isEqualTo(2713310158)
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