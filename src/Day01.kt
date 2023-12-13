fun main() {
    fun part1(input: List<String>): Int =
        input.map { rawLine: String ->
            rawLine.filter { char: Char ->
                char.isDigit()
            }
        }.filter(String::isNotEmpty)
        .map {
            "${it[0]}${it[it.length - 1]}"
        }.sumOf { it.toInt() }

    fun part2(input: List<String>): Int = input.sumOf(::calibrationValue)


    // test if implementation meets criteria from the description, like:
    val testInputPart1 = readInput("Day01_part1_test")
    check(part1(testInputPart1) == 142)

    val testInputPart2 = readInput("Day01_part2_test")
    check(part2(testInputPart2) == 281)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}

private fun calibrationValue(line: String): Int {
    val firstDigit = findFirstDigit(line)
    val lastDigit = findLastDigit(line)

    return firstDigit * 10 + lastDigit
}

private val words = listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")

private fun findFirstDigit(line: String): Int {
    val firstWordPair = line.findAnyOf(words)
    val firstWordIndex = firstWordPair?.first ?: Int.MAX_VALUE
    val firstDigitIndex = line.indexOfFirst { it.isDigit() }
        .let { if (it < 0) { Int.MAX_VALUE } else { it } }

    return if (firstWordIndex < firstDigitIndex) {
        getWordDigit(firstWordPair!!)
    } else {
        getDigitFromIndex(line, firstDigitIndex)
    }
}

private fun findLastDigit(line: String): Int {
    val lastWordPair = line.findLastAnyOf(words)
    val lastWordIndex = lastWordPair?.first ?: Int.MIN_VALUE
    val lastDigitIndex = line.indexOfLast { it.isDigit() }
        .let { if (it < 0) { Int.MIN_VALUE } else { it } }

    return if (lastWordIndex > lastDigitIndex) {
        getWordDigit(lastWordPair!!)
    } else {
        getDigitFromIndex(line, lastDigitIndex)
    }
}

private fun getWordDigit(wordPair: Pair<Int, String>) = getWordDigit(wordPair.second)

private fun getWordDigit(word: String) = words.indexOf(word) + 1

private fun getDigitFromIndex(line: String, index: Int) = line[index].digitToInt()
