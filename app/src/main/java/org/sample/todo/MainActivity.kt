package org.sample.todo

import android.os.Bundle
import android.view.View
import com.minivac.mini.R
import kotlinx.android.synthetic.main.activity_main.*
import org.sample.todo.core.flux.FluxActivity
import mini.TaskStatus
import mini.onNextTerminalState
import mini.select
import javax.inject.Inject

class MainActivity : FluxActivity() {

    @Inject lateinit var testStore: TestStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeInterface()
        startStoreChanges()
    }

    private fun startStoreChanges() {
        testStore.flowable()
            .select { it.text }
            .subscribe { miniText.text = it }
            .track()

        testStore.flowable()
            .select { it.getTextTask }
            .subscribe {
                when (it.status) {
                    TaskStatus.RUNNING -> progressBar.visibility = View.VISIBLE
                    TaskStatus.SUCCESS -> progressBar.visibility = View.GONE
                    TaskStatus.ERROR -> progressBar.visibility = View.GONE
                }
            }.track()
    }

    private fun initializeInterface() {
        dispatchButton.setOnClickListener { dispatcher.dispatch(ChangeTextAction("Feels good man")) }
        startBgTaskButton.setOnClickListener { dispatcher.dispatch(GetDataAction()) }
        navigateButton.setOnClickListener { navigate() }
    }

    private fun navigate() {
        progressBar.visibility = View.VISIBLE
        dispatcher.dispatch(NavigationAction())

        testStore.flowable()
            .onNextTerminalState(taskMapFn = { it.navigationTask },
                successFn = {
                    progressBar.visibility = View.GONE
                    startActivity(SecondActivity.newIntent(this))
                },
                failureFn = { progressBar.visibility = View.GONE })
            .track()
    }
}