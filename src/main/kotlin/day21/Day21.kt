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
    println("For $filename th monkey with name '${root.name}' yells '${root.resolve()}'")
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

fun <E> buildChildren(
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

fun String.toOperation(): (Long, Long) -> Long = when (this) {
    "+"  -> Long::plus
    "-"  -> Long::minus
    "*"  -> Long::times
    "/"  -> Long::div
    else -> throw IllegalStateException("Unrecognized operator '$this'")
}

fun String.toBigDecimalOp(): (BigDecimal, BigDecimal) -> BigDecimal = when (this) {
    "+"  -> BigDecimal::plus
    "-"  -> BigDecimal::minus
    "*"  -> BigDecimal::times
    "/"  -> BigDecimal::div
    else -> throw IllegalStateException("Unrecognized operator '$this'")
}

sealed class Node<E>(open val name: String) {

    data class Leaf<E>(override val name: String, val value: E) : Node<E>(name)
    data class Vertex<E>(override val name: String, val op: (E, E) -> E, val lchild: Node<E>, val rchild: Node<E>) :
        Node<E>(name)
}

fun part2(filename: String) {
    val (_, _, l, r) = buildTree(filename, String::toBigDecimal, String::toBigDecimalOp) as Node.Vertex<BigDecimal>
    val humnName = "humn"
    val (humn, noHumn) = if (l.find { it.name == humnName } != null) l to r else r to l

    val target = noHumn.resolve()

    println("\n\nStarting Newton approximation")
    val x = newtonApproximation(humnName, humn, target)

    println("For $filename I will shout: $x")

}


private fun newtonApproximation(nodeToReplaceName: String, root: Node<BigDecimal>, target: BigDecimal): BigDecimal {
    fun f(x: BigDecimal) = root.replace(Node.Leaf(nodeToReplaceName, x)) { it.name == nodeToReplaceName }
        .resolve() - target  // Newtons method finds a root of f, so we subtract our target here

    fun derivativeF(x: BigDecimal, h: BigDecimal = 1e-10.toBigDecimal()) = (f(x+h) - f(x)) / h

    fun abs(x: BigDecimal) = if(x < BigDecimal.ZERO) -x else x

    val x0 = 1.toBigDecimal().setScale(64)
    val xs = mutableListOf(x0)
    val fxs = mutableListOf(f(x0))

    var n = 0
    while (true){
        val (prevX, prevFX) = xs.last() to fxs.last()
        val newX = prevX - prevFX / derivativeF(prevX)
        xs.add(newX)
        fxs.add(f(newX))

        println("n=$n:\n> x_$n=$prevX \n> x_${n+1}=$newX with f(x_${n+1})=${fxs.last()}\n> delta=${abs(prevFX)}\n")
        if(abs(prevFX) < 1e-10.toBigDecimal()){
            break
        }
        n++
    }

    return xs.last().setScale(0, RoundingMode.HALF_EVEN)
}

fun <E> Node<E>.find(selector: (Node<E>) -> Boolean): Node<E>? = if (selector(this)) this else when (this) {
    is Node.Leaf   -> null
    is Node.Vertex -> lchild.find(selector) ?: rchild.find(selector)
}

fun <E> Node<E>.contains(element: Node<E>) = this.find { it == element } != null

fun <E> Node<E>.replace(replacement: Node<E>, selector: (Node<E>) -> Boolean): Node<E> =
    if (selector(this)) replacement else when (this) {
        is Node.Leaf<E>   -> this
        is Node.Vertex<E> -> this.copy(
            lchild = lchild.replace(replacement, selector),
            rchild = rchild.replace(replacement, selector)
        )
    }
