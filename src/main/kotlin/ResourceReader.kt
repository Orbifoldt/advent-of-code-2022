import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.util.stream.Stream

object ResourceReader {

    fun readBuffered(filename: String): BufferedReader =
        (javaClass.getResourceAsStream(filename) ?: throw FileNotFoundException(filename))
            .bufferedReader()

    fun readString(filename: String): String = readBuffered(filename).use { it.readText() }

    fun readLines(filename: String): Stream<String> = readBuffered(filename).let { bufferedReader ->
        bufferedReader.lines()
            .onClose {
                try {
                    bufferedReader.close()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
    }
}