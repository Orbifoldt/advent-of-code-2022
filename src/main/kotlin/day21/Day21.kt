package day21

import ResourceReader
import java.math.BigDecimal
import java.math.RoundingMode

fun main() {
    part1("example-script.txt")
    part1("my-script.txt")
    part2("example-script.txt")
    part2("my-script.txt")
}

fun part1(filename: String) {
    val root = buildTree(filename, String::toLong, String::toOperation)
    println("For $filename the monkey with name '${root.name}' yells '${root.resolve()}'")
}

sealed class Node<E>(open val name: String) {
    data class Leaf<E>(override val name: String, val value: E) : Node<E>(name)
    data class Vertex<E>(override val name: String, val op: (E, E) -> E, val lchild: Node<E>, val rchild: Node<E>) :
        Node<E>(name)
}

fun <E> Node<E>.resolve(): E = when (this) {
    is Node.Leaf   -> value
    is Node.Vertex -> op(lchild.resolve(), rchild.resolve())
}

private fun <E> buildTree(filename: String, valueParser: (String) -> E, operationParser: (String) -> ((E, E) -> E))
        : Node<E> {
    val lines = ResourceReader.readLines("day21/$filename").map(String::trim).toList()
    return buildChildren(lines.find { it.startsWith("root") }!!, lines, valueParser, operationParser)
}

private val constantRegex = Regex("(\\w{4}): (-?\\d+)")
private val operationRegex = Regex("(\\w{4}): (\\w{4}) ([+|\\-|*|/]) (\\w{4})")

private fun <E> buildChildren(
    input: String,
    allLines: List<String>,
    valueParser: (String) -> E,
    operationParser: (String) -> ((E, E) -> E),
): Node<E> = when {
    input.matches(constantRegex)  -> constantRegex.matchEntire(input)!!.groupValues.let { (_, name, value) ->
        Node.Leaf(name = name, value = valueParser(value))
    }

    input.matches(operationRegex) -> operationRegex.matchEntire(input)!!.groupValues.let { (_, name, l, op, r) ->
        Node.Vertex(
            name = name,
            op = operationParser(op),
            lchild = buildChildren(allLines.find { it.startsWith(l) }!!, allLines, valueParser, operationParser),
            rchild = buildChildren(allLines.find { it.startsWith(r) }!!, allLines, valueParser, operationParser)
        )
    }

    else                          -> throw IllegalArgumentException("Cannot parse in the line '$input'")
}

private fun String.toOperation(): (Long, Long) -> Long = when (this) {
    "+"  -> Long::plus
    "-"  -> Long::minus
    "*"  -> Long::times
    "/"  -> Long::div
    else -> throw IllegalStateException("Unrecognized operator '$this'")
}

fun part2(filename: String) {
    val (_, _, l, r) = buildTree(filename, String::toBigDecimal, String::toBigDecimalOp) as Node.Vertex<BigDecimal>
    val humn = "humn"
    val (humnBranch, noHumnBranch) = if (l.find { it.name == humn } != null) l to r else r to l

    // goal: we want to change our leaf such that humnBranch.resolve() == noHumnBranch.resolve()
    val target = noHumnBranch.resolve()

    println("\n\nStarting Newton approximation")
    val z = newtonApproximation { x -> humnBranch.replace(Node.Leaf(humn, x)) { it.name == humn }.resolve() - target }
    // Note that we subtracted the target from the value we get from resolving the humn part of the tree, this is
    // because Newton methods helps us find roots (zeroes) of a function.

    println("For $filename I will shout: ${z.setScale(0, RoundingMode.HALF_EVEN)}")
}

fun String.toBigDecimalOp(): (BigDecimal, BigDecimal) -> BigDecimal = when (this) {
    "+"  -> BigDecimal::plus
    "-"  -> BigDecimal::minus
    "*"  -> BigDecimal::times
    "/"  -> BigDecimal::div
    else -> throw IllegalStateException("Unrecognized operator '$this'")
}

fun <E> Node<E>.find(selector: (Node<E>) -> Boolean): Node<E>? = if (selector(this)) this else when (this) {
    is Node.Leaf   -> null
    is Node.Vertex -> lchild.find(selector) ?: rchild.find(selector)
}

fun <E> Node<E>.replace(replacement: Node<E>, selector: (Node<E>) -> Boolean): Node<E> =
    if (selector(this)) replacement else when (this) {
        is Node.Leaf<E>   -> this
        is Node.Vertex<E> -> this.copy(
            lchild = lchild.replace(replacement, selector),
            rchild = rchild.replace(replacement, selector)
        )
    }

private fun newtonApproximation(f: (BigDecimal) -> BigDecimal): BigDecimal {
    val x0 = 1.toBigDecimal().setScale(64)
    val xs = mutableListOf(x0)
    val fxs = mutableListOf(f(x0))

    var n = 0
    while (abs(fxs.last()) > 1e-10.toBigDecimal()) {
        val (prevX, prevFX) = xs.last() to fxs.last()
        val newX = prevX - prevFX / derivative(f = f)(prevX)
        xs.add(newX)
        fxs.add(f(newX))

        println("n=$n:\n> x_$n=$prevX \n> x_${n + 1}=$newX with f(x_${n + 1})=${fxs.last()}\n> delta=${abs(prevFX)}\n")
        n++
    }
    return xs.last()
}

fun derivative(h: BigDecimal = 1e-10.toBigDecimal(), f: (BigDecimal) -> BigDecimal): (BigDecimal) -> BigDecimal =
    { x -> (f(x + h) - f(x)) / h }

private fun abs(x: BigDecimal) = if (x < BigDecimal.ZERO) -x else x
