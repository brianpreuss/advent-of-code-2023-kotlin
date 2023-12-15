import kotlin.math.pow

fun main() {
    fun calculateTotalWinnings(game: List<Pair<Hand, Int>>) = game.sortedBy { it.first.strength }
        .mapIndexed { index, handBid ->
            handBid.second * (index + 1)
        }.sum()

    fun part1(input: List<String>) = calculateTotalWinnings(parse(input, 1))

    fun part2(input: List<String>) = calculateTotalWinnings(parse(input, 2))

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 6440)
    check(part2(testInput) == 5905)

    val input = readInput("Day07")
    part1(input).println()
    part2(input).println()

}

val allCardsPart1 = mutableListOf('2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A')
val allCardsPart2 = mutableListOf('J', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'Q', 'K', 'A')

val base = allCardsPart1.size.toDouble()

enum class Type {
    HIGH_CARD, ONE_PAIR, TWO_PAIRS, THREE_OF_A_KIND, FULL_HOUSE, FOUR_OF_A_KIND, FIVE_OF_A_KIND
}

data class Hand(val cards: String, val type: Type, val strength: Double) {
    companion object {
        fun parse(cards: String, part: Int): Hand {
            val type = calculateType(cards, part)

            val strength = calculateStrength(cards, type, part)

            return Hand(cards, type, strength)
        }

        private fun calculateType(cards: String, part: Int): Type {
            val cardCounts = cards.groupingBy { it }.eachCount().toMutableMap()

            if (part == 2) {
                var jokerCount = cardCounts['J'] ?: 0
                if (jokerCount > 0) {
                    cardCounts.remove('J')

                    val maxCardCount = cardCounts.maxByOrNull { it.value }

                    if (maxCardCount == null) {
                        cardCounts['J'] = jokerCount
                    } else {
                        cardCounts[maxCardCount.key] = maxCardCount.value + jokerCount
                    }
                }
            }

            var indiviualCardCount = cardCounts.keys.size

            return when (indiviualCardCount) {
                1 -> Type.FIVE_OF_A_KIND
                2 -> {
                    // four-of-a-kind AAAAB
                    // full-house     AAABB
                    val maxCards = cardCounts.values.max()
                    when (maxCards) {
                        4 -> Type.FOUR_OF_A_KIND // AAAAB
                        3 -> Type.FULL_HOUSE     // AAABB
                        else -> error("Invalid card count")
                    }
                }
                3 -> {
                    val maxCards = cardCounts.values.max()
                    if (maxCards == 3) {
                        Type.THREE_OF_A_KIND
                    } else {
                        Type.TWO_PAIRS
                    }
                }
                4 -> Type.ONE_PAIR // AABCD
                5 -> Type.HIGH_CARD // ABCDE
                else -> error("Invalid card count")
            }
        }

        private fun calculateStrength(cards: String, type: Type, part: Int): Double {
            val cardPositionsValue = cards.foldRightIndexed(0.0) { cardIndex, card, acc ->
                val exponent = cards.length - cardIndex - 1
                val multiplicand = base.pow(exponent)
                val multiplier = if (part == 1) allCardsPart1.indexOf(card) else allCardsPart2.indexOf(card)
                val cardValue =  multiplicand.times(multiplier)
                acc + cardValue
            }

            val typeValue = base.pow(cards.length).times(type.ordinal)

            return cardPositionsValue + typeValue
        }
    }
}

private fun parse(input: List<String>, part: Int): List<Pair<Hand, Int>> =
    input.map { line ->
        val parts = line.split(" ")
        val hand = Hand.parse(parts[0], part)
        val bid = parts[1].toInt()

        Pair(hand, bid)
    }
