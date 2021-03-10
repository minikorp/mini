package com.example.sample.another

import com.example.sample.MainState
import com.example.sample.SessionState
import com.example.sample.WorkoutState
import com.minikorp.mini.State

@State
data class RootState(
        val main: MainState = MainState(),
        val session: SessionState = SessionState(),
        val workout: WorkoutState = WorkoutState()
)