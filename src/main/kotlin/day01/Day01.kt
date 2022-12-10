package day01

import ResourceReader
import java.util.Deque
import java.util.LinkedList

fun main() {
    val elfCalories = countCaloriesPerElf("my-elves.txt")
    println("The maximum calories carried by any elf is: ${elfCalories.maxOrNull()!!}")
    println("The sum of calories carried by the top 3 is: ${elfCalories.sorted().takeLast(3).sum()}")

    val elfCaloriesV2 = countCaloriesPerElfV2("my-elves.txt")
    println("The maximum calories carried by any elf is: ${elfCaloriesV2.maxOrNull()!!}")
    println("The sum of calories carried by the top 3 is: ${elfCaloriesV2.sorted().takeLast(3).sum()}")
}

fun countCaloriesPerElf(filename: String): Deque<Int> {
    val caloriesCarriedByElves: Deque<Int> = LinkedList(listOf(0))
    ResourceReader.readLines("day01/$filename").forEach {
        if (it.isNotBlank()) {
            caloriesCarriedByElves.addLast(caloriesCarriedByElves.removeLast() + it.toInt())
        } else {
            caloriesCarriedByElves.addLast(0)
        }
    }
    return caloriesCarriedByElves
}

fun countCaloriesPerElfV2(filename: String): List<Int> =
    ResourceReader.readString("day01/$filename").split("\\n\\s?\\n".toRegex())  // to handle \r\n line breaks
        .map {
            it.trim()  // removes trailing \r's
                .lines().sumOf { itemCalorie -> itemCalorie.toInt() }
        }