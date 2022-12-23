package day07

sealed interface Fs {
    val name: String
    val size: Int
}

data class File(override val name: String, override val size: Int) : Fs

data class Dir(
    override val name: String,
    val parent: Dir?,
    val children: MutableList<Fs> = mutableListOf(),
) : Fs {
    override val size get() = if (_size < 1) recalculateSize() else _size
    private var _size = -1

    val dirs get() = children.filterIsInstance<Dir>()
    val files get() = children.filterIsInstance<File>()

    fun recalculateSize(): Int = children.sumOf { it.size }.also { _size = it }

    override fun toString(): String {
        return "Dir(name=$name', parent=${parent?.name}, size=$size, children=${children.map { it.name }})"
    }
}