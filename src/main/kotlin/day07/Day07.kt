package day07

import ResourceReader
import java.util.Deque
import java.util.LinkedList

fun main() {
    part1("example-dirs.txt")
    part1("my-dirs.txt")
    part2("example-dirs.txt")
    part2("my-dirs.txt")
}

fun part1(filename: String) {
    val sum = parseToFileTree(filename).dfsList().filterIsInstance<Dir>().filter { it.size < 100_000 }.sumOf { it.size }
    println("The sum of all directories with size less than 100_000 is '$sum'")
}

private fun parseToFileTree(filename: String): Dir {
    val output: Deque<String> = LinkedList(ResourceReader.readLines("day07/$filename").toList().drop(1))
    val root = Dir("/", null)

    var current: Dir = root
    while (output.isNotEmpty()) {
        val line = output.pop()
        when {
            line.startsWith("$ cd") -> current = cd(line.substring(5), current)
            line.startsWith("$ ls") -> output.removeWhile { !it.startsWith("$ ") }.forEach(current::addFileOrDirs)
        }
    }
    return root
}

private fun cd(arg: String, current: Dir) =
    if (arg == "..") {
        current.parent ?: current
    } else {
        current.children.find { it.name == arg } as? Dir ?: throw IllegalArgumentException("Dir $arg not found!")
    }

private fun Dir.addFileOrDirs(string: String) {
    Regex("(\\w+|\\d+) (.+)").matchEntire(string)!!.groupValues.let { (_, typeSize, name) ->
        children.add(if (typeSize == "dir") Dir(name, this) else File(name, typeSize.toInt()))
    }
}

fun <E> Deque<E>.removeWhile(predicate: (E) -> Boolean): List<E> {
    val acc = mutableListOf<E>()
    while (true) {
        val head = peek() ?: return acc
        if (predicate(head)) {
            acc.add(pop())
        } else {
            return acc
        }
    }
}

fun Dir.dfsList(): List<Fs> = dirs.flatMap { it.dfsList() } + dirs + files


fun part2(filename: String) {
    val root = parseToFileTree(filename)
    val usedSpace = root.size
    val totalDiskSpace = 70_000_000
    val requiredFreeSpace = 30_000_000

    val dir = root.dfsList().filterIsInstance<Dir>().filter { totalDiskSpace - usedSpace + it.size > requiredFreeSpace }
        .minBy { it.size }
    println("Should delete $dir")
}