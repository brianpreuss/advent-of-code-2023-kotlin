fun main() {
    fun part1(input: List<String>): Long {
        val desertMap = parse(input)
        return desertMap.navigate(1)
    }

    fun part2(input: List<String>): Long {
        val desertMap = parse(input)
        return desertMap.navigate(2)
    }

    // test if implementation meets criteria from the description, like:
    val testInput1 = readInput("Day08_test1")
    check(part1(testInput1) == 2L)
    val testInput2 = readInput("Day08_test2")
    check(part1(testInput2) == 6L)

    val input = readInput("Day08")
    part1(input).println()
    part2(input).println()
}

private val regex = """([A-Z]{3}) = \(([A-Z]{3}), ([A-Z]{3})\)""".toRegex()

typealias Node = String

private fun parse (input: List<String>): DesertMap {
    val instructions = input.first()

    val network = input.drop(2).associate { line: String ->
        val nodes = regex.find(line)?.groupValues ?: listOf()
        nodes[1] to Pair(nodes[2], nodes[3])
    }

    return DesertMap(instructions, network)
}

data class DesertMap(
    val instructions: String,
    val network: Map<Node, Pair<Node, Node>>
) {
    fun navigate(part: Int): Long {
        var startNodes: List<Node> = if (part == 1) listOf("AAA") else network.keys.filter { it.endsWith("A") }
        val stepsPerNode = startNodes.associateWith { startNode: Node ->
            val steps = navigateNode(startNode, "Z")
            steps
        }

        return lcm(stepsPerNode.values)
    }

    private fun navigateNode(startNode: String = "AAA", endNodeEnd: String = "ZZZ"): Long {
        var currentNode = startNode
        var steps = 0L
        do {
            instructions.forEach { instruction: Char ->
                val ways = network[currentNode]!!
                currentNode = if (instruction == 'L') ways.left() else ways.right()
                steps++
            }
        } while (!currentNode.endsWith(endNodeEnd))

        return steps
    }

    private fun Pair<Node, Node>.left() = this.first
    private fun Pair<Node, Node>.right() = this.second
}

private fun lcm(numbers: Collection<Long>): Long =
    numbers.reduce { a, b -> lcm(a, b) }

private fun lcm(a: Long, b: Long): Long =
    a.times(b.div(gcd(a, b)))

private fun gcd(a: Long, b: Long): Long {
    var num1 = a
    var num2 = b
    while (num2 != 0L) {
        val tmp = num2
        num2 = num1 % num2
        num1 = tmp
    }
    return num1
}
