import java.io.FileNotFoundException

object ResourceReader {
    fun readLines(filename: String) =
        (javaClass.getResourceAsStream(filename) ?: throw FileNotFoundException(filename))
        .bufferedReader().lines()
}