
typealias MultiMap<K, V> = MutableMap<K, MutableList<V>>

fun <K, V> emptyMultiMap() = mutableMapOf<K, MutableList<V>>()
fun <K, V> MultiMap<K, V>.add(key: K, value: V) = this.apply { getOrPut(key) { mutableListOf() }.add(value) }
fun <K, V> List<Pair<K, V>>.toMultiMap() = fold(emptyMultiMap<K, V>()) { map, (k, v) -> map.add(k, v) }
