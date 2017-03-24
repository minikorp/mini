package org.sample.todo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.minivac.mini.R
import com.minivac.mini.dagger.AppComponent
import com.minivac.mini.flux.Action
import com.minivac.mini.flux.app
import com.minivac.mini.log.Grove

class MainActivity : AppCompatActivity() {

    val goSecond: View by lazy { findViewById(R.id.goSecondButton) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        goSecond.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

        val appComponent = app.findComponent<AppComponent>(AppComponent.NAME)
        appComponent.dispatcher().dispatch(DummyAction(3))
    }

    override fun onDestroy() {
        super.onDestroy()
        Grove.d { "Destroyed, finishing $isFinishing" }

    }

    data class DummyAction(val x: Int = 3) : Action
}