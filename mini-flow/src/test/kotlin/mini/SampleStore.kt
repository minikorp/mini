package mini

class SampleStore : Store<String>() {

    companion object {
        const val INITIAL_STATE = "__initial__"
    }

    override fun initialState(): String = INITIAL_STATE

    fun updateState(s: String) {
        newState = s
    }
}