fun main () {
    fun part1(input: List<String>): Int = input.sumOf{ playGame(it, 1) }

    fun part2(input: List<String>): Int = input.sumOf{ playGame(it, 2) }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 8)
    check(part2(testInput) == 2286)

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}

private val cubes = mapOf("red" to 12, "green" to 13, "blue" to 14)

private val regex = mapOf(
    "red" to """(\d+) red""".toRegex(),
    "green" to """(\d+) green""".toRegex(),
    "blue" to """(\d+) blue""".toRegex()
)

private fun playGame(gameLine: String, part: Int): Int {
    val splitted = gameLine.split(":")
    val gameIndex: Int = splitted[0].replace("Game ", "").toInt()
    val gameCubeSet: Map<String, Int> = splitted[1].split(";")
        .map { it.trim() }
        .map(::mapDrawCubeSet)
        .reduceRight(::maxCubeSet)

    return if (part == 1) {
        scoreGameCubeSetPartByIndex(gameIndex, gameCubeSet)
    } else {
        scoreGameCubeSetByPower(gameCubeSet)
    }
}

/**
 * Map a cube set string (e.g. "3 blue, 4 red") to a map ("blue" to 3, "red" to 4, "green" to 0)
 */
private fun mapDrawCubeSet(drawCubeSet: String): Map<String, Int> = drawCubeSet.split(",")
    .map { it.trim() }
    .map { cubeColorCount ->
        regex.mapValues {
            val regex = it.value
            val group = regex.matchEntire(cubeColorCount)?.groups?.get(1)
            group?.value?.toInt() ?: 0
        }
    }.reduceRight { leftCubeSet, rightCubeSet ->
        rightCubeSet.mapValues {
            it.value + leftCubeSet[it.key]!!
        }
    }

/**
 * Combines all cube sets of a game so that the max cube color counts are returned
 */
private fun maxCubeSet(leftCubeSet: Map<String, Int>, rightCubeSet: Map<String, Int>): Map<String, Int> =
    rightCubeSet.mapValues { rightCube ->
        val cubesRight = rightCube.value
        val cubesLeft = leftCubeSet[rightCube.key]!!
        if (cubesRight >= cubesLeft) {
            cubesRight
        } else {
            cubesLeft
        }
    }

/**
 * Scores a game cube set.
 * Returns the game index if all game cubes matches the given cubes
 */
private fun scoreGameCubeSetPartByIndex(gameIndex: Int, gameCubeSet: Map<String, Int>): Int {
    val matchingCubes = gameCubeSet.mapValues {
        it.value <= cubes[it.key]!!
    }

    val allCubesMatches = matchingCubes.values.reduce { left, right -> left && right }

    return if (allCubesMatches) {
        gameIndex
    } else {
        0
    }
}

private fun scoreGameCubeSetByPower(gameCubeSet: Map<String, Int>) =
    gameCubeSet.values.reduce { leftCount, rightCount -> leftCount * rightCount }
