import kotlin.math.max
import kotlin.math.min

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day11_test")
    check(Day11.part1(testInput) == 374L)
    check(Day11.part1(testInput, 10 - 1) == 1030L)
    check(Day11.part1(testInput, 100 -1) == 8410L)

    val input = readInput("Day11")
    Day11.part1(input).println()
    Day11.part2(input).println()
}

object Day11 {
    fun part1(input: List<String>, expansion: Int = 1) = shortestPath(input, expansion)

    fun part2(input: List<String>) = shortestPath(input, 1000000 - 1)

    private fun shortestPath(input: List<String>, expansion: Int = 1): Long {
        val galaxies = Parser.parse(input, expansion)
        val galaxyPairs = pairs(galaxies)

        return galaxyPairs.sumOf { (galaxy1: Galaxy, galaxy2: Galaxy) ->
            galaxy1.shortestPathTo(galaxy2)
        }
    }

    private fun pairs(galaxies: Map<Position, Galaxy>): List<Pair<Galaxy, Galaxy>> {
        data class Acc(
            val galaxies: MutableList<Galaxy> = mutableListOf(),
            val galaxyPairs: MutableList<Pair<Galaxy, Galaxy>> = mutableListOf()
        )

        val start = Acc(galaxies = galaxies.values.sortedBy { it.position }.toMutableList())

        val folded = galaxies.values.fold(start) { acc, galaxy ->
            acc.galaxies.remove(galaxy)

            acc.galaxies.forEach {
                acc.galaxyPairs.add(Pair(galaxy, it))
            }

            acc
        }
        return folded.galaxyPairs.sortedBy { it.first.position }.toList()
    }

    object Parser {
        fun parse(input: List<String>, expansion: Int): Map<Position, Galaxy> {
            var galaxies = mutableMapOf<Position, Galaxy>()
            var rowExpansion = 0L

            val columnGalaxyCounter = (1..input[0].length).associateWith { 0 }.toMutableMap()

            input.forEachIndexed { rowIndex: Int, line: String ->
                var rowGalaxyCounter = 0L
                line.forEachIndexed { columnIndex: Int, char: Char ->
                    if (char == '#') {
                        val galaxyPosition = Position(row = rowIndex + 1 + rowExpansion, column = columnIndex + 1L)
                        galaxies[galaxyPosition] = Galaxy(
                            number = galaxies.size + 1,
                            position = galaxyPosition,
                            originalPosition = galaxyPosition.copy(row = galaxyPosition.row - rowExpansion)
                        )

                        rowGalaxyCounter += 1
                        columnGalaxyCounter[columnIndex + 1] = columnGalaxyCounter[columnIndex + 1]!! + 1
                    }
                }

                if (rowGalaxyCounter == 0L) {
                    rowExpansion += expansion
                }
            }

            val emptyColumns = columnGalaxyCounter.filter { it.value == 0 }

            emptyColumns.entries.map {
                it.key
            }.sortedByDescending { it }
                .forEachIndexed { index, emptyColumn ->
                    galaxies.keys.filter { galaxyPosition ->
                        galaxyPosition.column > emptyColumn
                    }.forEach { oldGalaxyPosition ->
                        val newGalaxyPosition = oldGalaxyPosition.copy(column = oldGalaxyPosition.column + expansion)
                        val galaxy = galaxies.remove(oldGalaxyPosition)!!
                        galaxies[newGalaxyPosition] = galaxy.copy(position = newGalaxyPosition)
                    }
                }

            return galaxies
        }
    }

    data class Galaxy(val number: Int, val position: Position, val originalPosition: Position) {
        fun shortestPathTo(other: Galaxy) = position.shortestPath(other.position)
    }

    data class Position(val row: Long, val column: Long): Comparable<Position> {
        override fun compareTo(other: Position): Int {
            val rowCompare = row.compareTo(other.row)

            return if (rowCompare == 0) {
                column.compareTo(other.column)
            } else {
                rowCompare
            }
        }

        override fun toString() = "($row,$column)"

        fun shortestPath(other: Position): Long {
            val rowDiff = max(row, other.row) - min(row, other.row)
            val columnDiff = max(column, other.column) - min(column, other.column)
            return rowDiff + columnDiff
        }
    }
}
