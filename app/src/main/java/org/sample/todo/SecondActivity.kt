package org.sample.todo

import android.os.Bundle
import android.widget.Toast
import com.minivac.mini.dagger.ComponentFactory
import com.minivac.mini.dagger.DestroyStrategy
import com.minivac.mini.flux.FluxActivity
import com.minivac.mini.log.Grove

class SecondActivity : FluxActivity<FakeDaggerComponent>() {

    override val componentFactory = object : ComponentFactory<FakeDaggerComponent> {
        override fun createComponent() = FakeDaggerComponent()
        override val destroyStrategy = DestroyStrategy.REF_COUNT
        override val componentName: String = "dummy"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "hey", Toast.LENGTH_SHORT).show()
        Grove.d { "Created" }
    }
}