package org.sample.todo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.minivac.mini.R
import com.minivac.mini.flux.FluxActivity
import mini.Dispatcher
import javax.inject.Inject

class MainActivity : FluxActivity() {

    @Inject lateinit var dispatcher: Dispatcher
    @Inject lateinit var userStore: WarcraftStore

    val goSecond: TextView by lazy { findViewById<TextView>(R.id.goSecondButton) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        goSecond.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

        userStore
            .observe { goSecond.text = it.name }
        

        if (savedInstanceState == null) {
            dispatcher.dispatch(PlusUltraAction("${userStore.state.name} Hello", "Reactive"))
        }
    }
}
