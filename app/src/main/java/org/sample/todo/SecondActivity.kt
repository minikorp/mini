package org.sample.todo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.minivac.mini.log.Grove

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "hey", Toast.LENGTH_SHORT).show()
        Grove.d { "Created" }
    }
}