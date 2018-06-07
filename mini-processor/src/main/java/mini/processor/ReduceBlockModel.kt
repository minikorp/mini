package mini.processor

class ReduceBlockModel(val action: ActionModel, reducers: List<ReducerFuncModel>) {
    val methodCalls = reducers.map { StoreMethod(it) }.sortedWith(compareBy({it.priority}, {it.storeName}))
}