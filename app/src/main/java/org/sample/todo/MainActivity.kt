package org.sample.todo

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.minivac.mini.R
import com.minivac.mini.dagger.AppComponent
import com.minivac.mini.dagger.ComponentFactory
import com.minivac.mini.dagger.DestroyStrategy
import com.minivac.mini.flux.Action
import com.minivac.mini.flux.FluxActivity
import com.minivac.mini.flux.app

class MainActivity : FluxActivity<FakeDaggerComponent>() {

    override val componentFactory = object : ComponentFactory<FakeDaggerComponent> {
        override fun createComponent() = FakeDaggerComponent()
        override val destroyStrategy = DestroyStrategy.REF_COUNT
        override val componentName: String = "dummy"
    }

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

    data class DummyAction(val x: Int = 3) : Action
}
