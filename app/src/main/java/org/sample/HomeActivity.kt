package org.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import mini.Action
import mini.BaseAction

class HomeActivity : AppCompatActivity() {
    companion object {
        fun newIntent(context: Context): Intent = Intent(context, HomeActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
    }
}



@Action interface SomeWeirdAction
abstract class SampleAbstractAction : BaseAction()
class SampleAction : SomeWeirdAction, SampleAbstractAction()

@Action class BlehAction