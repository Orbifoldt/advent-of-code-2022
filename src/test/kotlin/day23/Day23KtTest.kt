package day23

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.awt.Point

class Day23KtTest {
    private val points = arrayOf(Point(1, 1), Point(0, 2), Point(1, 2), Point(2, 2), Point(1, 3))


    @Test
    fun `can parse file to points`() {
        val parsedPoints = parse("test-elves.txt")
        assertThat(parsedPoints.map { Point(it.x, it.y) }).containsExactlyInAnyOrder(*points)
    }

    @Test
    fun `should move into correct direction`() {
        assertThat(nextPosition(points[0], points.toHashSet(), Direction.values().toList())).isEqualTo(Point(1, 0))
        assertThat(nextPosition(points[1], points.toHashSet(), Direction.values().toList())).isEqualTo(Point(-1, 2))
        assertThat(nextPosition(points[2], points.toHashSet(), Direction.values().toList())).isEqualTo(Point(1, 2))
        assertThat(nextPosition(points[3], points.toHashSet(), Direction.values().toList())).isEqualTo(Point(3, 2))
        assertThat(nextPosition(points[4], points.toHashSet(), Direction.values().toList())).isEqualTo(Point(1, 4))
    }
}