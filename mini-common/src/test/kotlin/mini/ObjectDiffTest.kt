package mini

import org.junit.Test

class ObjectDiffTest {

    //TODO: Complete these tests

    @Test
    fun `diff plain object`() {
        println(ObjectDiff.computeDiff("a", "b"))
        println(ObjectDiff.computeDiff(3, 4))
        println(ObjectDiff.computeDiff(3.14, 4.345f))
        println(ObjectDiff.computeDiff(3.14, 3.14))
    }

    @Test
    fun `diff plain class`() {

        data class Sample(val x: Int, val y: String)
        println(ObjectDiff.computeDiff(Sample(4, "abc"), Sample(4, "efg")))
    }

    @Test
    fun `diff list`() {
        val a = listOf("a", "b", "c")
        val b = listOf("a", "c", "f")
        //println(ObjectDiff.findDiff(Sample(4, "abc"), Sample(4, "efg")))
    }

    @Test
    fun `diff map`() {
        val a = mapOf("same" to "1", "removed" to "2")
        val b = mapOf("same" to "1", "added" to "3")
        println(ObjectDiff.computeDiff(a, b))
    }
}