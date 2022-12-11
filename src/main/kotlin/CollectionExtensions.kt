

fun Iterable<Long>.product(): Long = fold(1L) { product, item -> product * item}
fun Iterable<Int>.product(): Int = fold(1) { product, item -> product * item}