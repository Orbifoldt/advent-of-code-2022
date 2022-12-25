package day25

import ResourceReader
import java.math.RoundingMode
import kotlin.math.log
import kotlin.math.pow

fun main() {
    part1("example-fuel.txt")
    part1("my-fuel.txt")
}

data class Snafu(private val digits: IntArray) : Number() {
    constructor(string: String) : this(string.toCharArray().map(::toDigit).toIntArray())
    constructor(base10Value: Long) : this(computeSnafuDigits(base10Value))

    operator fun plus(other: Snafu) = Snafu(this.toLong() + other.toLong())

    override fun toInt() = toLong().toInt()
    override fun toLong() = digits.reversed().withIndex().fold(0L) { acc, (i, d) -> acc + d * 5L.pow(i) }
    override fun toShort() = toLong().toShort()
    override fun toByte() = toLong().toByte()
    override fun toChar() = toInt().toChar()
    override fun toDouble() = digits.reversed().withIndex().fold(0.0) { acc, (i, d) -> acc + d * 5.0.pow(i) }
    override fun toFloat() = toDouble().toFloat()

    override fun toString() = digits.dropWhile { it == 0 }.map(::toChar).joinToString("")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Snafu
        return digits.contentEquals(other.digits)
    }

    override fun hashCode(): Int = digits.contentHashCode()

    companion object {
        fun toDigit(c: Char): Int = when (c) {
            '='  -> -2
            '-'  -> -1
            '0'  -> 0
            '1'  -> 1
            '2'  -> 2
            else -> throw IllegalArgumentException("'$c' is not a valid SNAFU digit")
        }

        fun toChar(d: Int) = listOf('=', '-', '0', '1', '2')[d+2]

        private fun Long.pow(n: Int) = this.toDouble().pow(n).toLong()

        private fun computeSnafuDigits(x: Long): IntArray {

            val snafuDigits = IntArray(logRoundUp(x, base = 5) + 1)
            x.toString(5).reversed().map {it.digitToInt() }.mapIndexed { i, d ->
                val cur = d + snafuDigits[i]
                if (cur > 2){
                    snafuDigits[i] = cur - 5
                    snafuDigits[i+1] += 1
                } else {
                    snafuDigits[i] += d
                }
            }
            return snafuDigits.reversed().toIntArray()
        }

        private fun logRoundUp(x: Number, base: Int = 5) = log(x.toDouble(), base.toDouble())
            .toBigDecimal().setScale(0, RoundingMode.UP).toInt()
    }
}

fun part1(filename: String) {
    val sum = ResourceReader.readLines("day25/$filename")
        .map { Snafu(it.trim()) }
        .reduce { t, u -> t + u  }
    println("The sum of fuel amounts in $filename is '${sum.orElseThrow()}'")
}
