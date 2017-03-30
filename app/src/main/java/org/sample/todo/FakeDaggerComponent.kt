package org.sample.todo

import com.minivac.mini.dagger.DisposableComponent
import com.minivac.mini.log.Grove


class FakeDaggerComponent : DisposableComponent {
    override fun dispose() {
        Grove.d { "I am now disposed :( " }
    }
}