package day05

import ResourceReader
import java.util.Stack

fun main(){
    part1("example-crates.txt")
    part1("my-crates.txt")
    part2("example-crates.txt")
    part2("my-crates.txt")
}

fun part1(filename: String){
    val (stacks, instructions) = parseInput(filename)
    craneMover9000Execute(instructions, stacks)
    readTops(stacks, 9000)
}

private fun parseInput(filename: String): Pair<List<Stack<Char>>, List<List<Int>>> {
    val (stacksInput, instructionsInput) = ResourceReader.readString("day05/$filename")
        .split(Regex("\\n\\s?\\n"))
    return parseStartPosition(stacksInput).getStacks() to parseInstructions(instructionsInput)
}

fun parseStartPosition(input: String) = input.trimEnd().lines().dropLast(1)
    .map { row -> row.chunked(4) { it[1] } }

fun List<List<Char>>.getStacks(): List<Stack<Char>> = List(last().size) { index ->
    Stack<Char>().also { stack ->
        this.reversed().forEach { row -> if(row.size > index && row[index] != ' ' ) { stack.push(row[index]) } }
    }
}

fun parseInstructions(input: String) = input.trim().lines().map { line ->
    Regex("move (\\d+) from (\\d+) to (\\d+).*").matchEntire(line)!!.groupValues.drop(1).map(String::toInt)
}

private fun craneMover9000Execute(instructions: List<List<Int>>, stacks: List<Stack<Char>>) =
    instructions.forEach { (amount, from, to) ->
        repeat(amount) {
            stacks[to - 1].push(stacks[from - 1].pop())
        }
    }

private fun readTops(stacks: List<Stack<Char>>, machineVersion: Int) {
    val tops = stacks.map { it.pop() }.joinToString("")
    println("After using CrateMover$machineVersion the tops of the stacks read: '$tops'")
}


fun part2(filename: String){
    val (stacks, instructions) = parseInput(filename)
    craneMover9001Execute(instructions, stacks)
    readTops(stacks, 9001)
}

@OptIn(ExperimentalStdlibApi::class)
private fun craneMover9001Execute(instructions: List<List<Int>>, stacks: List<Stack<Char>>) =
    instructions.forEach { (amount, from, to) ->
        (0..<amount).map { stacks[from - 1].pop() }
            .reversed()
            .forEach { stacks[to - 1].push(it) }
    }
