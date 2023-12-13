fun main() {
    fun part1(input: List<String>): Int = parse(input, 1)
        .map {
            findBestRaceOptionsPart1(it).size
        }.reduce { left, right ->
            left * right
        }

    fun part2(input: List<String>): Long = parse(input, 2)
        .map {
            findBestRaceOptionsPart2(it)
        }.reduce { left, right ->
            left * right
        }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput) == 288)
    check(part2(testInput) == 71503L)

    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}

private val numberRegex = """(\d+)""".toRegex()

private fun parse(input: List<String>, part: Int): List<Race> {
    val numbers = if (part == 1) {
        parseNumbersPart1(input)
    } else {
        parseNumbersPart2(input)
    }

    return numbers.foldIndexed(mutableListOf<Race>()) { numberListIndex: Int, acc: MutableList<Race>, next: List<Long> ->
        // first line: times
        // second line: distances
        next.forEachIndexed { index, number ->
            if (numberListIndex == 0) {
                acc.add(Race(duration = number, recordDistance = 0))
            } else {
                acc[index] = acc[index].copy(recordDistance = number)
            }
        }

        acc
    }.toList()
}

private fun parseNumbersPart1(input: List<String>) =
    input.map { line ->
        numberRegex.findAll(line)
            .map { it.value.toLong() }
            .toList()
    }

private fun parseNumbersPart2(input: List<String>): List<List<Long>> =
    input.map { line ->
        listOf(
            numberRegex.findAll(line)
                .map { it.value.toLong() }
                .fold("") { left, right ->
                    left + right.toString()
                }.toLong()
        )
    }

private fun findBestRaceOptionsPart1(race: Race): List<RaceOption> =
    (0 .. race.duration).mapNotNull { buttonPressTime ->
        val currentRaceOption = RaceOption(buttonPressTime, (race.duration - buttonPressTime) * buttonPressTime)
        if (currentRaceOption.distance > race.recordDistance) {
            currentRaceOption
        } else {
            null
        }
    }

private fun findBestRaceOptionsPart2(race: Race): Long =
    (0 .. race.duration).reduceIndexed { buttonPressTime, acc, _ ->
        val distance = (race.duration - buttonPressTime) * buttonPressTime

        if (distance > race.recordDistance) {
            acc + 1
        } else {
            acc
        }
    }

data class Race(val duration: Long, val recordDistance: Long)

data class RaceOption(val buttonPressTime: Long, val distance: Long)
