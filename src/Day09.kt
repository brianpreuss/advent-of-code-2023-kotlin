fun main() {
    fun part1(input: List<String>): Long {
        return parse(input)
            .sumOf(::extrapolate)
    }

    fun part2(input: List<String>): Long {
        return parse(input)
            .sumOf { extrapolate(it.reversed()) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day09_test")
    check(part1(testInput) == 114L)
    check(part2(testInput) == 2L)

    val input = readInput("Day09")
    part1(input).println()
    part2(input).println()
}

private val regex = """(-?\d+)""".toRegex()

private fun parse(input: List<String>): List<List<Long>> =
    input.map { line: String ->
        regex.findAll(line)
            .map { it.value.toLong() }
            .toList()
    }

private fun extrapolate(values: List<Long>): Long {
    if (values.all { it == 0L }) {
        return 0L
    }

    val differences = values.windowed(2, 1)
        .map { it.last() - it.first() }

    return values.last() + extrapolate(differences)
}
