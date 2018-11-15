package org.sample.utils

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.view.View
import android.widget.Toast

//Toasting
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, message, duration).show()
fun Context.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, getString(message), duration).show()

//View
fun View.makeVisible() = run { visibility = View.VISIBLE }
fun View.makeInvisible() = run { visibility = View.INVISIBLE }
fun View.makeGone() = run { visibility = View.GONE }
fun View.isVisible() = visibility == View.VISIBLE
fun View.isInvisible() = visibility == View.INVISIBLE
fun View.isGone() = visibility == View.GONE