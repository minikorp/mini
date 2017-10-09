package com.minivac.mini.misc

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import java.util.*
import kotlin.collections.ArrayList


private val random = Random()

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

//Keyboard

fun Activity.hideKeyboard(focusView: View? = this.currentFocus, force: Boolean = false) {
    if (focusView == null) return
    val flags = if (force) 0 else InputMethodManager.HIDE_IMPLICIT_ONLY
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(focusView.windowToken, flags)
}

//Toasting

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT)
        = Toast.makeText(this, message, duration).show()

fun Context.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT)
        = Toast.makeText(this, getString(message), duration).show()

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT)
        = this.activity.toast(message, duration)

fun Fragment.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT)
        = this.activity.toast(message, duration)

//Bundle

fun <V> Map<String, V>.toBundle(bundle: Bundle = Bundle()): Bundle = bundle.apply {
    forEach {
        val k = it.key
        val v = it.value
        when (v) {
            is IBinder -> putBinder(k, v)
            is Bundle -> putBundle(k, v)
            is Byte -> putByte(k, v)
            is ByteArray -> putByteArray(k, v)
            is Char -> putChar(k, v)
            is CharArray -> putCharArray(k, v)
            is CharSequence -> putCharSequence(k, v)
            is Float -> putFloat(k, v)
            is FloatArray -> putFloatArray(k, v)
            is Parcelable -> putParcelable(k, v)
            is Short -> putShort(k, v)
            is ShortArray -> putShortArray(k, v)
            else -> throw IllegalArgumentException("$v is of a type that is not currently supported")
        }
    }
}

fun newBundle(fn: BundleBuilder.() -> Unit): Bundle {
    val builder = BundleBuilder()
    fn(builder)
    return builder.bundle
}

class BundleBuilder {
    val bundle = Bundle()
    infix fun String.toValue(value: Int) = bundle.putInt(this, value)
    infix fun String.toValue(value: Long) = bundle.putLong(this, value)
    infix fun String.toValue(value: String) = bundle.putString(this, value)
    infix fun String.toValue(value: Boolean) = bundle.putBoolean(this, value)
    infix fun String.toValue(value: Parcelable) = bundle.putParcelable(this, value)
}

//Resources

fun String.toUri(): Uri = Uri.parse(this)

fun Context.colorCompat(@ColorRes colorResource: Int): Int =
        ContextCompat.getColor(this, colorResource)
fun Fragment.colorCompat(@ColorRes colorResource: Int): Int =
        ContextCompat.getColor(activity, colorResource)

fun randomColor(alpha: Int = 255): Int =
        Color.argb(alpha, random.nextInt(256), random.nextInt(256), random.nextInt(256))

private val point = Point()
fun Context.calculateScreenSize(): Pair<Int, Int> {
    val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    display.getSize(point)
    val width = point.x
    val height = point.y
    return width to height
}

fun Pair<Int, Int>.toSize(): Size = Size(this.first, this.second)

//Database

inline fun <T> Cursor.mapToList(mapper: (cursor: Cursor) -> T): List<T> {
    val size = count
    val list = ArrayList<T>(size)
    while (moveToNext()) {
        list.add(mapper(this))
    }
    return list
}