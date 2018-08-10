package org.sample.todo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.minivac.mini.R
import org.sample.todo.core.flux.FluxActivity

class SecondActivity : FluxActivity() {

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, SecondActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_two)
    }
}
