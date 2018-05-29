package mini.processor

class ReduceBlockModel(val actionName: String, reducers: List<ReducerFuncModel>) {
    val methodCalls = reducers.map { StoreMethod(it) }.sortedWith(compareBy({it.priority}, {it.storeName}))
}