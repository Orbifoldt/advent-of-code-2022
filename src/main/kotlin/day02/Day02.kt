package day02

import ResourceReader

fun main() {
    println("--- Part One ---")
    println("The score according to the strategy is: ${part1("example-rps.txt")}")
    println("The score according to the strategy is: ${part1("my-rps.txt")}")

    println("--- Part Two ---")
    println("The score according to the strategy is: ${part2("example-rps.txt")}")
    println("The score according to the strategy is: ${part2("my-rps.txt")}")
}

fun part1(filename: String): Int =
    ResourceReader.readColumns(filename).map { (othersMove, myMove, _) ->
        score(myMove.mapXyzToMove(), othersMove.mapAbcToMove())
    }.reduce(Int::plus).get()

enum class Move(val score: Int) { ROCK(1), PAPER(2), SCISSORS(3) }
enum class Outcome(val score: Int) { WIN(6), DRAW(3), LOSE(0) }

fun String.mapAbcToMove() = when (this) {
    "A"  -> Move.ROCK
    "B"  -> Move.PAPER
    "C"  -> Move.SCISSORS
    else -> throw IllegalArgumentException("Invalid input for other player: '$this'")
}

fun String.mapXyzToMove() = when (this) {
    "X"  -> Move.ROCK
    "Y"  -> Move.PAPER
    "Z"  -> Move.SCISSORS
    else -> throw IllegalArgumentException("Invalid input for other player: '$this'")
}

fun score(myMove: Move, otherMove: Move): Int = myMove.score + determineOutcome(myMove, otherMove).score

val outcomeMatrix = listOf( /* Outcome is from perspective of the row */
    /*                        ROCK         PAPER         SCISSORS */
    /* ROCK     */ listOf(Outcome.DRAW, Outcome.LOSE, Outcome.WIN),
    /* PAPER    */ listOf(Outcome.WIN, Outcome.DRAW, Outcome.LOSE),
    /* SCISSORS */ listOf(Outcome.LOSE, Outcome.WIN, Outcome.DRAW),
)

fun determineOutcome(myMove: Move, otherMove: Move) = outcomeMatrix[myMove.ordinal][otherMove.ordinal]

fun part2(filename: String): Int =
    ResourceReader.readColumns(filename).map { (othersMove, desiredOutcome, _) ->
        val other = othersMove.mapAbcToMove()
        val mine = determineMyMove(desiredOutcome.mapXyzToOutcome(), other)
        score(mine, other)
    }.reduce(Int::plus).get()

fun String.mapXyzToOutcome() = when (this) {
    "X"  -> Outcome.LOSE
    "Y"  -> Outcome.DRAW
    "Z"  -> Outcome.WIN
    else -> throw IllegalArgumentException("Invalid input for other player: '$this'")
}

fun determineMyMove(desiredOutcome: Outcome, otherMove: Move) =
    outcomeMatrix.map { it[otherMove.ordinal] }  // get the column
        .withIndex().find { (_, outcome) -> outcome == desiredOutcome }!!.index  // row index corresponding to my move
        .let { Move.values()[it] }
