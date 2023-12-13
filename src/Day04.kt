import kotlin.math.pow

fun main() {
    fun part1(input: List<String>): Int =
        input.map { Card(it) }
            .sumOf { it.points }

    fun part2(input: List<String>): Int {
        val cards = input.map { Card(it) }
        return totalCards(cards)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 30)

    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}

private fun totalCards(originalCards: List<Card>) : Int {
    val totalCards = originalCards.associate {
        Pair(it.number, 1)
    }.toMutableMap()

    originalCards.forEach { originalCard ->
        val cardCount = totalCards[originalCard.number] ?: 0

        (1..cardCount).forEach { _ ->
            (originalCard.number + 1..originalCard.number + originalCard.copies).forEach { cardNumberToBeCopied ->
                val previousCount = totalCards[cardNumberToBeCopied] ?: 0
                totalCards[cardNumberToBeCopied] = previousCount + 1
            }
        }
    }

    return totalCards.values.sum()
}

data class Card(val number: Int, val winningNumbers: List<Int>, val numbersYouHave: List<Int>) {
    val points: Int get() = List(winningNumbersYouHave().size) { countOfWinningNumbers ->
        2.0.pow(countOfWinningNumbers).toInt()
    }.lastOrNull() ?: 0

    val copies: Int get() {
        val winningNumbersYouHave = winningNumbersYouHave()
        val copiesWon = (1..winningNumbersYouHave.size).map { Card(number, winningNumbers, numbersYouHave) }

        return copiesWon.size
    }

    private fun winningNumbersYouHave() = numbersYouHave.filter {
        winningNumbers.contains(it)
    }

    companion object {
        operator fun invoke(rawLine: String): Card {
            val splittedByColon = rawLine.split(":")
            val cardNumber = parseCardNumber(splittedByColon[0])
            val splittedByPipe = splittedByColon[1].split("|").map { it.trim() }
            val winningNumbers = parseNumbers(splittedByPipe[0])
            val numbersYouHave = parseNumbers(splittedByPipe[1])

            return Card(cardNumber, winningNumbers, numbersYouHave)
        }

        private fun parseCardNumber(cardNumberString: String) =
            cardNumberString.replace("Card ", "").trim().toInt()

        private fun parseNumbers(numbersString: String) =
            numbersString.split(" ")
                .map { it.trim() }
                .filterNot { it.isEmpty() }
                .map { it.toInt() }
                .toList()
    }
}
