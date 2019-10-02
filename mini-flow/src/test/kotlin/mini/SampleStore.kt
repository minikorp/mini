package mini

class SampleStore : Store<String>() {
    override fun initialState(): String = "initial"

    fun updateState(s: String) {
        newState = s
    }
}