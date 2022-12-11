

fun Iterable<Long>.product(): Long = reduce(Long::times)
fun Iterable<Int>.product(): Int = reduce(Int::times)