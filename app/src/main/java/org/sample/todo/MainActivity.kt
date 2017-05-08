package org.sample.todo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.minivac.mini.R
import com.minivac.mini.flux.FluxActivity
import javax.inject.Inject

class MainActivity : FluxActivity<UserComponent>() {

    @Inject lateinit var userStore: UserStore

    override val componentFactory = UserComponentFactory

    val goSecond: TextView by lazy { findViewById(R.id.goSecondButton) as TextView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        goSecond.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

        userStore.flowable()
                .subscribe { goSecond.text = it.name }
                .track()

        if (savedInstanceState == null) {
            dispatcher.dispatch(LoginUserAction("${userStore.state.name} Hello", "Reactive"))
        }
    }
}
