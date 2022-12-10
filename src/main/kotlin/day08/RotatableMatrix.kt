package day08

// TODO: use this class instead of the typealias and extension functions
class RotatableMatrix<T>(private val matrix: Matrix<T>) : Iterable<T>, Collection<T>{
    override val size get() = height * width
    val height get() = matrix.size
    val width get() = matrix[0].size

    val currentRotation get() = _currentRotation

    private var _currentRotation: Int = 0

    operator fun get(y: Int, x: Int) = when (_currentRotation) {
        90 -> matrix[x, height - 1 - y]
        180 -> matrix[height - 1 - y, width - 1 - x]
        270 -> matrix[width - 1 - x, y]
        else -> matrix[y, x]
    }

    private fun rotate(degrees: Int) {
        _currentRotation = (_currentRotation + degrees) % 360
    }
    fun rotate90() = rotate(90)
    fun rotate180() = rotate(180)

    fun rotate270() = rotate(270)

    companion object {
        fun fromString(string: String) = RotatableMatrix(
            string.lines()
                .map {
                    it.toList().map(Char::digitToInt)
                }
        )
    }

    fun flatten() = matrix.flatten()

    override fun isEmpty()= this.flatten().isEmpty()

    override fun containsAll(elements: Collection<T>) = this.flatten().containsAll(elements)

    override fun contains(element: T) = this.flatten().contains(element)

    override fun iterator() = this.flatten().iterator()
}