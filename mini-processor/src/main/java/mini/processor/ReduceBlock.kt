package mini.processor

class ReduceBlock(val actionName: String, reducers: List<ReducerModelFunc>) {
    val methodCalls = reducers.map { StoreMethod(it) }.sortedWith(compareBy({it.priority}, {it.storeName}))
}