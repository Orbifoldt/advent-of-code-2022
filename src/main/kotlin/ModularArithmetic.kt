import kotlin.math.abs

fun Int.mod(n: Int) = Math.floorMod(this, n)

tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) abs(a) else gcd(b, a % b)
fun gcd(vararg aa: Long): Long = aa.reduce(::gcd)  // gcd(a, b, c) = gcd(gcd(a, b), c)
fun gcd(aa: Iterable<Long>): Long = aa.reduce(::gcd)

fun lcm(a: Long, b: Long): Long = abs(a * b) / gcd(a, b)
fun lcm(vararg aa: Long): Long = aa.reduce(::lcm)  // lcm(a, b, c) = lcm(lcm(a, b), c)
fun lcm(aa: Iterable<Long>): Long = aa.reduce(::lcm)

fun lcm(a: Int, b: Int): Int = lcm(a.toLong(), b.toLong()).toInt()
