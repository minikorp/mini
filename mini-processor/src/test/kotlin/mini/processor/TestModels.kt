package mini.processor

import mini.Action
import mini.Reducer
import mini.Store

interface GymAction : Action
data class PullUpAction(val value: Int) : Action
data class RestAction(val value: Int) : Action
data class DeadliftAction(val value: Int) : GymAction

data class BodyweightState(val value: Int = 0)
data class DeadliftState(val value: Int = 0)

class BodyweightStore : Store<BodyweightState>() {

    @Reducer
    fun doPullUps(action: PullUpAction): BodyweightState {
        return state.copy(value = action.value)
    }

    @Reducer
    fun rest(action: RestAction, oldState: BodyweightState): BodyweightState {
        return oldState.copy(value = action.value)
    }
}

class GymStore : Store<DeadliftState>() {

    @Reducer
    fun workout(action: GymAction): DeadliftState {
        return state.copy(value = 1)
    }

    @Reducer
    fun heavyLifting(action: DeadliftAction): DeadliftState {
        return state.copy(value = action.value)
    }

    @Reducer
    fun rest(action: RestAction): DeadliftState {
        return state.copy(value = action.value)
    }
}