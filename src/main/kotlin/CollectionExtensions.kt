

fun Iterable<Long>.product(): Long = reduce(Long::times)
fun Iterable<Int>.product(): Int = reduce(Int::times)

// Cartesian product
operator fun <T> Iterable<T>.times(other: Iterable<T>) = flatMap { a -> other.map { b -> a to b } }