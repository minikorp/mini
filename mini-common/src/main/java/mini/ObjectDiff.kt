package mini

import kotlin.reflect.full.memberProperties

private typealias DiffMap = MutableMap<Any, Any?>

@Suppress("FunctionName")
private fun DiffMap() = HashMap<Any, Any?>()

object ObjectDiff {

    fun <T : Any> computeDiff(a: T?, b: T?): String {
        val out = DiffMap()
        diffObject(a, b, ".", out)
        return out["."]?.toString() ?: ""
    }

    private fun <T : Any> diffObject(a: T?, b: T?, propertyName: String, diffMap: DiffMap) {
        if (a == b) return

        if (a == null || b == null) {
            diffMap[propertyName] = "$a => $b"
            return
        }

        when (a) {
            //Primitive types
            is String, is Number -> {
                diffMap[propertyName] = "$a => $b"
            }
            is Map<*, *>         -> {
                b as Map<*, *>
                val out = DiffMap()
                b.entries.forEach { (k, v) ->
                    //Key was added / changed
                    if (a[k] != v) {
                        //Object inside changed
                        diffObject(a[k], v, k.toString(), out)
                    }
                }

                a.entries.forEach { (k, _) ->
                    //Key was removed
                    if (!b.containsKey(k)) {
                        out[k ?: "null_key"] = a[k]
                    }
                }

                diffMap[propertyName] = out
            }
            else                 -> {
                if (a::class.isData) {
                    val props = a::class.memberProperties
                    val out = DiffMap()
                    props.forEach { prop ->
                        val aValue = prop.getter.call(a)
                        val bValue = prop.getter.call(b)
                        diffObject(aValue, bValue, prop.name, out)
                    }
                    diffMap[propertyName] = out
                } else {
                    //Any other type, just print them
                    diffMap[propertyName] = "$a => $b"
                }
            }
        }
    }
}