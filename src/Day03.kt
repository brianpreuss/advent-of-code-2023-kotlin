fun main() {
    fun part1(input: List<String>): Int = EngineSchematics(input).partNumbers.sumOf{ it.value }

    fun part2(input: List<String>): Int = EngineSchematics(input).gears.sumOf { it.ratio }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 4361)
    check(part2(testInput) == 467835)

    val input = readInput("Day03")
    part1(input).println() // 526404 - correct
    part2(input).println()
}

private const val NOT_A_SYMBOL = '.'
private const val GEAR_SYMBOL = '*'

data class EngineSchematics private constructor(val partNumbers: List<PartNumber>, val gears: List<Gear>) {
    companion object {
        operator fun invoke(rawLines: List<String>): EngineSchematics {
            val tempPartNumbers = mutableListOf<PartNumber>()
            val gearCandidates = mutableListOf<Position>()
            rawLines.forEachIndexed { lineIndex, rawLine ->
                val (linePartNumbers, lineGearCandidates) = processLine(rawLines, rawLine, lineIndex)
                tempPartNumbers.addAll(linePartNumbers)
                gearCandidates.addAll(lineGearCandidates)
            }

            val gears = gearCandidates.mapNotNull { gearCandidate ->
                val (candidateLine, candidateIndex) = gearCandidate.start
                val adjacentPartNumbers = tempPartNumbers.filter { partNumber ->
                    val lineInRange = partNumber.position.start.first in candidateLine - 1 .. candidateLine + 1
                    val indexInRange = (candidateIndex - 1 .. candidateIndex + 1).map {
                        it in partNumber.position.start.second .. partNumber.position.end.second
                    }.reduce { acc, b -> acc || b }

                    lineInRange && indexInRange
                }

                if (adjacentPartNumbers.size == 2)
                    Gear(adjacentPartNumbers.first(), adjacentPartNumbers.last())
                else
                    null
            }

            return EngineSchematics(tempPartNumbers.toList(), gears)
        }

        private fun processLine(
            rawLines: List<String>,
            currentLine: String,
            lineIndex: Int
        ): Pair<List<PartNumber>, List<Position>> {
            val tempLinePartNumbers = mutableListOf<PartNumber>()
            var currentFirstDigitIndex = Int.MAX_VALUE
            var isPartNumber = false
            val gearCandidates = mutableListOf<Position>()
            currentLine.forEachIndexed { charIndex, c ->
                if (c.isDigit()) {
                    currentFirstDigitIndex = currentFirstDigitIndex.coerceAtMost(charIndex)
                    val adjacentSymbolOnPreviousLine = isAdjacentSymbolOnPreviousLine(rawLines, lineIndex, charIndex)
                    val adjacentSymbolOnCurrentLine = scanForAdjacentSymbolOnCurrentLine(currentLine, charIndex)
                    val adjacentSymbolOnNextLine = isAdjacentSymbolOnNextLine(rawLines, lineIndex, charIndex)
                    isPartNumber = isPartNumber
                            || adjacentSymbolOnPreviousLine
                            || adjacentSymbolOnCurrentLine
                            || adjacentSymbolOnNextLine
                } else {
                    if (isPartNumber)  {
                        tempLinePartNumbers.add(
                            newPartNumber(
                                currentLine = currentLine,
                                currentLineIndex = lineIndex,
                                firstDigitIndex = currentFirstDigitIndex,
                                lastDigitIndex = charIndex
                            )
                        )
                        isPartNumber = false
                    }
                    currentFirstDigitIndex = Int.MAX_VALUE

                    if (c == GEAR_SYMBOL) {
                        gearCandidates.add(Position(start = Pair(lineIndex, charIndex), end = Pair(lineIndex, charIndex)))
                    }
                }

                if (isPartNumber && charIndex == currentLine.length - 1)  {
                    tempLinePartNumbers.add(
                        newPartNumber(
                            currentLine = currentLine,
                            currentLineIndex = lineIndex,
                            firstDigitIndex = currentFirstDigitIndex,
                            lastDigitIndex = charIndex + 1
                        )
                    )
                }
            }
            return Pair(tempLinePartNumbers.toList(), gearCandidates.toList())
        }

        private fun newPartNumber(
            currentLine: String,
            currentLineIndex: Int,
            firstDigitIndex: Int,
            lastDigitIndex: Int
        ) = PartNumber(
            value = currentLine.substring(firstDigitIndex, lastDigitIndex).toInt(),
            position = Position(
                start = Pair(currentLineIndex, firstDigitIndex),
                end = Pair(currentLineIndex, lastDigitIndex - 1)
            )
        )

        private fun isAdjacentSymbolOnPreviousLine(
            rawLines: List<String>,
            currentLineIndex: Int,
            currentCharIndex: Int
        ) = if (currentLineIndex > 0) {
            scanForAdjacentSymbolOnPreviousLine(rawLines[currentLineIndex - 1], currentCharIndex)
        } else {
            // return false if on first line
            false
        }

        private fun scanForAdjacentSymbolOnPreviousLine(previousLine: String, currentCharIndex: Int) =
            scanForAdjacentSymbolOnLine(previousLine, currentCharIndex, false)

        private fun scanForAdjacentSymbolOnCurrentLine(currentLine: String, currentCharIndex: Int): Boolean =
            scanForAdjacentSymbolOnLine(currentLine, currentCharIndex, true)

        private fun isAdjacentSymbolOnNextLine(
            rawLines: List<String>,
            currentLineIndex: Int,
            currentCharIndex: Int
        ) = if (currentLineIndex < rawLines.size - 1) {
            scanForAdjacentSymbolOnNextLine(rawLines[currentLineIndex + 1], currentCharIndex)
        } else {
            // return false if on last line
            false
        }

        private fun scanForAdjacentSymbolOnNextLine(nextLine: String, currentCharIndex: Int) =
            scanForAdjacentSymbolOnLine(nextLine, currentCharIndex, false)

        private fun scanForAdjacentSymbolOnLine(line: String, currentCharIndex: Int, currentLine: Boolean): Boolean {
            val previousChar = line.previousChar(currentCharIndex) ?: NOT_A_SYMBOL

            // treat current char as non symbol if on current line
            val currentChar = if (currentLine) {
                NOT_A_SYMBOL
            } else {
                line[currentCharIndex]
            }

            val nextChar = line.nextChar(currentCharIndex) ?: NOT_A_SYMBOL

            return previousChar.isSymbol() || currentChar.isSymbol() || nextChar.isSymbol()
        }

        private fun nextCharacterIsDigit(line: String, currentCharIndex: Int) =
            if (currentCharIndex < line.length -1 ) {
                line[currentCharIndex + 1].isDigit()
            } else {
                false
            }
    }
}

data class PartNumber(val value: Int, val position: Position)

data class Position(val start: Pair<Int, Int>, val end: Pair<Int, Int>)

data class Gear(val firstPartNumber: PartNumber, val secondPartNumber: PartNumber) {
    val ratio: Int get() = firstPartNumber.value * secondPartNumber.value
 }

private fun String.previousChar(index: Int): Char? = if (index > 0) {
    this[index - 1]
} else {
    null
}

private fun String.nextChar(index: Int): Char? = if (index < this.length - 1) {
    this[index + 1]
} else {
    null
}

private fun Char.isSymbol() = !this.isDigit() && this != NOT_A_SYMBOL
