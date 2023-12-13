import java.io.File
import kotlin.math.max
import kotlin.math.min

fun main() {
    val input = File("day05/input05.txt05.txt")
        .readLines()

    val debug = false

    val minLocationNumberSeedPart1 = findMinLocation(lines = input, part = 1, useBruteForce = false, debug = debug)
    printResult(result = minLocationNumberSeedPart1, debug = debug)
    // testInput05.txt -> 35
    // input05.txt -> 457535844


    val minLocationNumberSeedPart2 = findMinLocation(lines = input, part = 2, useBruteForce = false, debug = debug)
    printResult(result = minLocationNumberSeedPart2, debug = debug)
    // testInput05.txt -> 46
    // input05.txt -> OOM
    // input05.txt -> 41222968 (dauert lange) => jetzt schnell
}

private fun findMinLocation(lines: List<String>, part: Int, useBruteForce: Boolean, debug: Boolean): Long {
    var seedRanges = parseSeedRanges(lines[0], part)
    val rangesPerSections = parseSections(lines)

    return if (useBruteForce) {
        minByBruteForce(seedRanges, rangesPerSections)
    } else {
        minByLocationRanges(seedRanges, rangesPerSections, debug = debug)
    }
}

private fun parseSeedRanges(line: String, part: Int): List<LongRange> =
    if (part == 1) {
        parseNumbers(line).map {
            it .. it
        }
    } else {
        parseRanges(line)
    }.sortedBy { it.first }

private val numberRegex = """(\d+)""".toRegex()

private fun parseNumbers(seedsLine: String): List<Long> {
    return numberRegex.findAll(seedsLine)
        .map {
            it.value.toLong()
        }
        .toList()
}

private val rangeRegex = """(\d+) (\d+)""".toRegex()
private fun parseRanges(seedsLine: String): List<LongRange> {
    return rangeRegex.findAll(seedsLine)
        .map {
            val ranges = it.value.split(" ")
            val rangeStart = ranges[0].toLong()
            val rangeEnd = rangeStart + ranges[1].toLong() -1
            rangeStart.. rangeEnd
        }.toList()
}

private fun parseSections(lines: List<String>): Map<String, List<RangeOffset>> {
    val rangeOffsetsPerSections = mutableMapOf<String, MutableList<RangeOffset>>()
    var currentSection = ""

    lines.slice(2..< lines.size).forEach { line ->
         if (line.endsWith(":")) {
            currentSection = line.removeSuffix(" map:")
             rangeOffsetsPerSections[currentSection] = mutableListOf<RangeOffset>()
        } else if (line.isNotBlank()) {
            val rangeOffset = RangeOffset(line)
            val sectionRanges = rangeOffsetsPerSections[currentSection]!!
            sectionRanges.add(rangeOffset)
        }
    }

    return rangeOffsetsPerSections.mapValues {
        it.value.sortedBy { range -> range.sourceRange.first }
    }.toMap()
}

data class RangeOffset(val sourceRange: LongRange, val offset: Long) {
    fun map(source: Long): Long? =
        if (source in sourceRange) {
            source + offset
        } else {
            null
        }

    companion object {
        operator fun invoke(line: String): RangeOffset {
            val numbers = parseNumbers(line)
            val destinationRangeStart = numbers[0]
            val sourceRangeStart = numbers[1]
            val rangeLength = numbers[2]
            val sourceRange = sourceRangeStart..< sourceRangeStart + rangeLength
            return RangeOffset(
                sourceRange = sourceRange,
                offset = destinationRangeStart - sourceRangeStart
            )
        }
    }
}

private fun minByBruteForce(seedRanges: List<LongRange>, rangesPerSections: Map<String, List<RangeOffset>>): Long {
    var minLocation = Long.MAX_VALUE
    seedRanges.forEach { seedRange ->
        seedRange.forEach {seedNumber ->
            val locationNumber = toSeed(rangesPerSections, seedNumber).locationNumber

            if (locationNumber < minLocation) {
                minLocation = locationNumber
            }
        }
    }
    return minLocation
}

// important: seedRanges are ordered
// important: rangesPerSection entries are ordered by insertion
private fun minByLocationRanges(seedRanges: List<LongRange>, rangesPerSections: Map<String, List<RangeOffset>>, debug: Boolean): Long {
    var tmpRanges = seedRanges.toMutableList()
    var shiftedRanges = mutableListOf<LongRange>()
    var affectedRanges = mutableListOf<LongRange>()
    var remainingRanges = mutableListOf<LongRange>()

    rangesPerSections.forEach { rangesPerSection ->
        val sectionRangeOffsets = rangesPerSection.value
        if (debug) {
            println()
            println("processing section ${rangesPerSection.key}")
            println()

            println("current ranges $tmpRanges")
        }

        sectionRangeOffsets.forEach { rangeOffset ->
            println("=> processing rangeOffset $rangeOffset")

            println("--> tmpRanges is $tmpRanges")

            tmpRanges.forEach { tmpRange ->
                val maxStart = max(tmpRange.first, rangeOffset.sourceRange.first)
                val minLast = min(tmpRange.last, rangeOffset.sourceRange.last)

                if (maxStart > tmpRange.last || minLast < tmpRange.first) {
                    if (debug) {
                        println("current range $tmpRange is not affected by rangeOffset $rangeOffset")
                    }
                } else {
                    if (debug) {
                        println("current range $tmpRange is affected by rangeOffset $rangeOffset")
                    }
                    affectedRanges.add(tmpRange)

                    if (maxStart > tmpRange.first) {
                        val preRange = tmpRange.first ..< maxStart
                        remainingRanges.add(preRange)
                        if (debug) {
                            println("--> new pre range $preRange")
                        }
                    }

                    val intersection = maxStart .. minLast
                    val shiftedIntersectionRange = maxStart + rangeOffset.offset .. minLast + rangeOffset.offset
                    shiftedRanges.add(shiftedIntersectionRange)
                    if (debug) {
                        println("--> shifted intersection range $intersection to $shiftedIntersectionRange")
                    }

                    if (minLast < tmpRange.last) {
                        val postRange = minLast + 1 .. tmpRange.last
                        remainingRanges.add(postRange)
                        if (debug) {
                            println("--> new post range $postRange")
                        }
                    }
                }
            } // for each tmp ranges
            if (debug) {
                println("--> removing affected ranges $affectedRanges")
            }
            tmpRanges.removeAll(affectedRanges)
            affectedRanges = mutableListOf()
            if (debug) {
                println("--> adding remaining ranges $remainingRanges")
            }
            tmpRanges.addAll(remainingRanges)
            remainingRanges = mutableListOf()
        } // for each section range offset

        tmpRanges.addAll(shiftedRanges)
        tmpRanges.sortBy { it.first }
        shiftedRanges = mutableListOf()
    } // for each section

    return tmpRanges.first()!!.first
}

private fun toSeed(rangeOffsetPerSection: Map<String, List<RangeOffset>>, number: Long): Seed {
    val soilNumber = mapSourceToDestination(rangeOffsetPerSection["seed-to-soil"]!!, number)
    val fertilizerNumber = mapSourceToDestination(rangeOffsetPerSection["soil-to-fertilizer"]!!, soilNumber)
    val waterNumber = mapSourceToDestination(rangeOffsetPerSection["fertilizer-to-water"]!!, fertilizerNumber)
    val lightNumber = mapSourceToDestination(rangeOffsetPerSection["water-to-light"]!!, waterNumber)
    val temperatureNumber = mapSourceToDestination(rangeOffsetPerSection["light-to-temperature"]!!, lightNumber)
    val humidityNumber = mapSourceToDestination(rangeOffsetPerSection["temperature-to-humidity"]!!, temperatureNumber)
    val locationNumber = mapSourceToDestination(rangeOffsetPerSection["humidity-to-location"]!!, humidityNumber)

    return Seed(
        number = number,
        soilNumber = soilNumber,
        fertilizerNumber = fertilizerNumber,
        waterNumber = waterNumber,
        lightNumber = lightNumber,
        temperatureNumber = temperatureNumber,
        humidityNumber = humidityNumber,
        locationNumber = locationNumber
    )
}

private fun mapSourceToDestination(rangeOffsets: List<RangeOffset>, sourceNumber: Long): Long =
    rangeOffsets.firstNotNullOfOrNull { rangeOffset ->
        rangeOffset.map(sourceNumber)
    } ?: sourceNumber

data class Seed(
    val number: Long,
    val soilNumber: Long,
    val fertilizerNumber: Long,
    val waterNumber: Long,
    val lightNumber: Long,
    val temperatureNumber: Long,
    val humidityNumber: Long,
    val locationNumber: Long
)

private fun printResult(result: Long, debug: Boolean) {
    if (debug) {
        println()
        println("*************************")
    }

    println(result)

    if (debug) {
        println("*************************")
    }
}
