package day17

enum class Rock(val coords: List<Coord>, val height: Int = coords.maxBy { it.second }.second + 1) {
    HORIZONTAL_LINE(
        listOf(
            0 to 0, 1 to 0, 2 to 0, 3 to 0
        )
    ),
    PLUS(
        listOf(
            /*   */ 1 to 2,
            0 to 1, 1 to 1, 2 to 1,
            /*   */ 1 to 0
        )
    ),
    HOOK(
        listOf(
            /*           */ 2 to 2,
            /*           */ 2 to 1,
            0 to 0, 1 to 0, 2 to 0
        )
    ),
    VERTICAL_LINE(
        listOf(
            0 to 0,
            0 to 1,
            0 to 2,
            0 to 3
        )
    ),
    SQUARE(
        listOf(
            0 to 1, 1 to 1,
            0 to 0, 1 to 0
        )
    );
}