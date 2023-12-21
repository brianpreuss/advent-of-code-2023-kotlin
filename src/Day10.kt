fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput1 = readInput("Day10_test1")
    check(Day10.part1(testInput1).also { println("check part 1: $it") } == 8)

    val testInput2a = readInput("Day10_test2a")
    check(Day10.part2(testInput2a).also { println("check part 2a $it") } == 4)

    val testInput2b = readInput("Day10_test2b")
    check(Day10.part2(testInput2b).also { println("check part 2b $it") } == 8)

    val testInput2c = readInput("Day10_test2c")
    check(Day10.part2(testInput2c).also { println("check part 2c $it") } == 10)

    val input = readInput("Day10")
    Day10.part1(input).println()
    Day10.part2(input).println()
}

object Day10 {
    fun part1(input: List<String>): Int {
        return Parser.parse(input).farestSteps()
    }

    fun part2(input: List<String>): Int {
        val pipes = Parser.parse(input)
        val route = pipes.route()
        val candidates = pipes.pipes.minus(route.keys)

        val pipesInside = candidates.filterValues { candidate: Pipe ->
            val routePipesOnRow =
                route.filterKeys {
                    routePosition: Position -> routePosition.row == candidate.position.row
                }.toSortedMap()
            countCrossedInnerBoundariesByHorizontalRay(candidate, route) % 2 > 0
        }

        return pipesInside.size
    }

    object Parser {
        fun parse(lines: List<String>): Pipes {
            val pipes = lines.mapIndexed { rowIndex, line ->
                parseLine(rowIndex, line)
            }.flatten()

            return Pipes(pipes, lines.size, lines[0].length)
        }

        private fun parseLine(rowIndex: Int, line: String): List<Pipe> =
            line.mapIndexed { columnIndex, c ->
                Pipe(c, Position(rowIndex + 1, columnIndex + 1))
            }
    }

    sealed interface Pipe {
        val encoding: Char
        val position: Position
        val innerBoundary: Pair<Char, Char>

        val canGoUp: Boolean
        val canGoDown: Boolean
        val canGoLeft: Boolean
        val canGoRight: Boolean

        fun propagateInnerBoundary(previousPipe: Pipe): Pipe

        fun crossHorizontalRay(): Int

        /** - */
        data class Horizontal(
            override val position: Position,
            override val innerBoundary: Pair<Char, Char> = Pair('-', '-')
        ) : Pipe {
            override val encoding: Char get() = '-'
            override val canGoUp: Boolean get() = false
            override val canGoDown: Boolean get() = false
            override val canGoLeft: Boolean get() = true
            override val canGoRight: Boolean get() = true

            override fun toString() = "'$encoding'@$position-$innerBoundary"

            override fun propagateInnerBoundary(previousPipe: Pipe) =
                this.copy(innerBoundary = Pair(previousPipe.innerBoundary.first, 'X'))

            override fun crossHorizontalRay() = 0
        }

        /** | */
        data class Vertical(
            override val position: Position,
            override val innerBoundary: Pair<Char, Char> = Pair('-', '-')
        ) : Pipe {
            override val encoding: Char get() = '|'
            override val canGoUp: Boolean get() = true
            override val canGoDown: Boolean get() = true
            override val canGoLeft: Boolean get() = false
            override val canGoRight: Boolean get() = false

            override fun toString() = "'$encoding'@$position-$innerBoundary"

            override fun propagateInnerBoundary(previousPipe: Pipe) =
                this.copy(innerBoundary = Pair('X', previousPipe.innerBoundary.second))

            override fun crossHorizontalRay() = 1
        }

        /** F */
        data class DownRight(
            override val position: Position,
            override val innerBoundary: Pair<Char, Char> = Pair('-', '-'),
        ) : Pipe {
            override val encoding: Char get() = 'F'
            override val canGoUp: Boolean get() = false
            override val canGoDown: Boolean get() = true
            override val canGoLeft: Boolean get() = false
            override val canGoRight: Boolean get() = true

            override fun toString() = "'$encoding'@$position-$innerBoundary"

            override fun propagateInnerBoundary(previousPipe: Pipe): Pipe {
                val prev = if (previousPipe is Start) previousPipe.pipe!! else previousPipe

                val first = when (prev) {
                    is Vertical, is UpRight -> if (prev.innerBoundary.second == 'E') 'N' else 'S'
                    else -> prev.innerBoundary.first // -, J, 7
                }
                val second = when (prev) {
                    is Horizontal, is DownLeft -> if (prev.innerBoundary.first == 'N') 'E' else 'W'
                    else -> prev.innerBoundary.second
                }
                return this.copy(innerBoundary = Pair(first, second))
            }

            override fun crossHorizontalRay() = if (innerBoundary.second == 'W') 1 else 0
        }

        /** 7 */
        data class DownLeft(
            override val position: Position,
            override val innerBoundary: Pair<Char, Char> = Pair('-', '-')
        ) : Pipe {
            override val encoding: Char get() = '7'
            override val canGoUp: Boolean get() = false
            override val canGoDown: Boolean get() = true
            override val canGoLeft: Boolean get() = true
            override val canGoRight: Boolean get() = false

            override fun toString() = "'$encoding'@$position-$innerBoundary"

            override fun propagateInnerBoundary(previousPipe: Pipe): Pipe {
                val prev = if (previousPipe is Start) previousPipe.pipe!! else previousPipe
                val first = when (prev) {
                    is Vertical, is UpLeft -> if (prev.innerBoundary.second == 'E') 'S' else 'N'
                    else -> prev.innerBoundary.first
                }
                val second = when (prev) {
                    is Horizontal, is DownRight -> if (prev.innerBoundary.first == 'N') 'W' else 'E'
                    else -> prev.innerBoundary.second
                }
                return this.copy(innerBoundary = Pair(first, second))
            }

            override fun crossHorizontalRay() = if (innerBoundary.second == 'E') 1 else 0
        }

        /** L */
        data class UpRight(
            override val position: Position,
            override val innerBoundary: Pair<Char, Char> = Pair('-', '-')
        ) : Pipe {
            override val encoding: Char get() = 'L'
            override val canGoUp: Boolean get() = true
            override val canGoDown: Boolean get() = false
            override val canGoLeft: Boolean get() = false
            override val canGoRight: Boolean get() = true

            override fun propagateInnerBoundary(previousPipe: Pipe): Pipe {
                val prev = if (previousPipe is Start) previousPipe.pipe!! else previousPipe
                val first = when (prev) {
                    is Vertical, is DownRight -> if (prev.innerBoundary.second == 'E') 'S' else 'N'
                    else -> prev.innerBoundary.first
                }
                val second = when (prev) {
                    is Horizontal, is UpLeft -> if (prev.innerBoundary.first == 'N') 'W' else 'E'
                    else -> prev.innerBoundary.second
                }
                return this.copy(innerBoundary = Pair(first, second))
            }

            override fun crossHorizontalRay() = if (innerBoundary.second == 'W') 1 else 0

            override fun toString() = "'$encoding'@$position-$innerBoundary"
        }

        /** J */
        data class UpLeft(
            override val position: Position,
            override val innerBoundary: Pair<Char, Char> = Pair('-', '-')
        ) : Pipe {
            override val encoding: Char get() = 'J'
            override val canGoUp: Boolean get() = true
            override val canGoDown: Boolean get() = false
            override val canGoLeft: Boolean get() = true
            override val canGoRight: Boolean get() = false

            override fun propagateInnerBoundary(previousPipe: Pipe): Pipe {
                val prev = if (previousPipe is Start) previousPipe.pipe!! else previousPipe
                val first = when (prev) {
                    is Vertical, is DownLeft -> if (prev.innerBoundary.second == 'E') 'N' else 'S'
                    else -> prev.innerBoundary.first
                }
                val second = when (previousPipe) {
                    is Horizontal, is UpRight -> if (prev.innerBoundary.first == 'N') 'E' else 'W'
                    else -> prev.innerBoundary.second
                }
                return this.copy(innerBoundary = Pair(first, second))
            }

            override fun crossHorizontalRay() = if (innerBoundary.second == 'E') 1 else 0

            override fun toString() = "'$encoding'@$position-$innerBoundary"
        }

        /** S */
        data class Start(
            override val encoding: Char = 'S',
            override val position: Position,
            val pipe: Pipe? = null
        ) : Pipe {
            override val canGoUp: Boolean get() = pipe?.canGoUp ?: true
            override val canGoDown: Boolean get() = pipe?.canGoDown ?: true
            override val canGoLeft: Boolean get() = pipe?.canGoLeft ?: true
            override val canGoRight: Boolean get() = pipe?.canGoRight ?: true

            override val innerBoundary: Pair<Char, Char>
                get() = pipe?.innerBoundary ?: Pair('-', '-')

            override fun toString() = "'$encoding'/'${pipe?.encoding ?: 'X'}'@$position-$innerBoundary"

            override fun propagateInnerBoundary(previousPipe: Pipe) =
                this.copy(pipe = pipe!!.propagateInnerBoundary(previousPipe))

            override fun crossHorizontalRay() = pipe!!.crossHorizontalRay()
        }

        /** . */
        data class Point(
            override val position: Position,
            override val innerBoundary: Pair<Char, Char> = Pair('-', '-')
        ) : Pipe {
            override val encoding: Char get() = '.'

            override val canGoUp: Boolean get() = false
            override val canGoDown: Boolean get() = false
            override val canGoLeft: Boolean get() = false
            override val canGoRight: Boolean get() = false

            override fun toString() = "'$encoding'@$position-$innerBoundary"

            override fun propagateInnerBoundary(previousPipe: Pipe) = error("A point can't have a boundary.")

            override fun crossHorizontalRay() = 0
        }

        companion object {
            operator fun invoke(encoding: Char, position: Position, innerBoundary: Pair<Char, Char> = Pair('-', '-')) =
                when (encoding) {
                    '-' -> Horizontal(position = position, innerBoundary = innerBoundary)
                    '|' -> Vertical(position = position, innerBoundary = innerBoundary)
                    'F' -> DownRight(position = position, innerBoundary = innerBoundary)
                    '7' -> DownLeft(position = position, innerBoundary = innerBoundary)
                    'L' -> UpRight(position = position, innerBoundary = innerBoundary)
                    'J' -> UpLeft(position = position, innerBoundary = innerBoundary)
                    'S' -> Start(position = position)
                    '.' -> Point(position = position, innerBoundary = innerBoundary)
                    else -> error("Unknown char $encoding")
                }
        }
    }

    data class Position(val row: Int, val column: Int): Comparable<Position> {
        override fun compareTo(other: Position): Int {
            val rowCompare = row.compareTo(other.row)

            return if (rowCompare != 0) rowCompare else column.compareTo(other.column)
        }

        override fun toString() = "($row,$column)"
    }

    // data class Boundary?

    data class Pipes(private val pipesList: List<Pipe>, private val maxRows: Int, private val maxColumns: Int) {
        private val start: Pipe
        private var _pipes: Map<Position, Pipe> = pipesList.associateBy { it.position }
        val pipes: Map<Position, Pipe> get() = _pipes

        init {
            val originalStart = pipesList.single { it is Pipe.Start } as Pipe.Start
            val replacedStart = replaceStart(originalStart)
            _pipes = _pipes.mapValues { entry ->
                if (entry.value == originalStart) {
                    replacedStart
                } else {
                    entry.value
                }
            }

            start = replacedStart
        }

        private fun replaceStart(start: Pipe.Start): Pipe {
            val canGoUp = goUp(start)?.canGoDown == true
            val canGoDown = goDown(start)?.canGoUp == true
            val canGoLeft = goLeft(start)?.canGoRight == true
            val canGoRight = goRight(start)?.canGoLeft == true

            val pipe =
                if (canGoUp && canGoDown) {
                    Pipe.Vertical(start.position)
                } else if (canGoUp && canGoLeft) {
                    Pipe.UpLeft(start.position)
                } else if (canGoUp && canGoRight) {
                    Pipe.UpRight(start.position)
                } else if (canGoDown && canGoLeft) {
                    Pipe.DownLeft(start.position)
                } else if (canGoDown && canGoRight) {
                    Pipe.DownRight(start.position)
                } else if (canGoLeft && canGoRight) {
                    Pipe.Horizontal(start.position)
                } else {
                    error("Unable to replace start position")
                }

            return start.copy(pipe = pipe)
        }
        
        private fun goUp(pipe: Pipe) =
            if (pipe.canGoUp) {
                pipes[pipe.position.copy(row = pipe.position.row - 1)]
            } else {
                null
            }

        private fun goDown(pipe: Pipe) =
            if (pipe.canGoDown) {
                pipes[pipe.position.copy(row = pipe.position.row + 1)]
            } else {
                null
            }

        private fun goLeft(pipe: Pipe) =
            if (pipe.canGoLeft) {
                pipes[pipe.position.copy(column = pipe.position.column - 1)]
            } else {
                null
            }

        private fun goRight(pipe: Pipe) =
            if (pipe.canGoRight) {
                pipes[pipe.position.copy(column = pipe.position.column + 1)]
            } else {
                null
            }

        fun farestSteps(): Int {
            return route().size / 2
        }

        fun route(): Map<Position, Pipe> {
            val route = mutableListOf<Pipe>()
            var previousPipe: Pipe? = null
            var currentPipe = start
            var upperLeftCorner: Pipe? = null

            do {
                upperLeftCorner = findUpperLeftCorner(upperLeftCorner, currentPipe)!!
                route.add(currentPipe)
                val nextPipe = nextPipe(currentPipe, previousPipe)
                previousPipe = currentPipe
                currentPipe = nextPipe
            } while (nextPipe != start)

            val routeWithBoundaries = mutableListOf<Pipe>()

            upperLeftCorner = enrichUpperLeftCornerWithInnerBoundaries(upperLeftCorner!!)

            route.map { route: Pipe ->

            }

            currentPipe = upperLeftCorner
            previousPipe = null

            do {
                routeWithBoundaries.add(currentPipe)
                var nextPipe = nextPipe(currentPipe, previousPipe)
                nextPipe = nextPipe.propagateInnerBoundary(currentPipe)
                previousPipe = currentPipe
                currentPipe = nextPipe
            } while (nextPipe != upperLeftCorner)

            return routeWithBoundaries.associateBy { it.position }
        }

        private fun findUpperLeftCorner(upperLeft: Pipe?, current: Pipe): Pipe? =
            when (current) {
                is Pipe.DownRight, is Pipe.Start -> {
                    if (upperLeft == null) {
                        current
                    } else if (current.position.row < (upperLeft.position.row)) {
                        current
                    } else if (
                        current.position.row == upperLeft.position.row &&
                        current.position.column < upperLeft.position.column
                    ) {
                        current
                    } else {
                        upperLeft
                    }
                }
                else -> upperLeft
            }

        private fun nextPipe(currentPipe: Pipe, previousPipe: Pipe?): Pipe {
            val upPipe = goUp(currentPipe)
            if (upPipe != null && upPipe.position != previousPipe?.position) {
                return upPipe
            }

            val downPipe = goDown(currentPipe)
            if (downPipe != null && downPipe.position != previousPipe?.position) {
                return downPipe
            }

            val leftPipe = goLeft(currentPipe)
            if (leftPipe != null && leftPipe.position != previousPipe?.position) {
                return leftPipe
            }

            return goRight(currentPipe)
                ?: error(currentPipe)
        }

        private fun enrichUpperLeftCornerWithInnerBoundaries(upperLeftCorner: Pipe) =
            when (upperLeftCorner) {
                is Pipe.DownRight ->
                    upperLeftCorner.copy(innerBoundary = Pair('S', 'W'))
                is Pipe.Start -> {
                    val pipe = upperLeftCorner.pipe as Pipe.DownRight
                    upperLeftCorner.copy(pipe = pipe.copy(innerBoundary = Pair('S', 'W')))
                }
                else -> error("Illegal upper left corner $upperLeftCorner")
            }
    }

    private fun countCrossedInnerBoundariesByHorizontalRay(candidate: Pipe, route: Map<Position, Pipe>): Int =
        (1 .. candidate.position.column).sumOf { column: Int ->
            val routePipe = route[candidate.position.copy(column = column)]
            routePipe?.crossHorizontalRay() ?: 0
        }
}
