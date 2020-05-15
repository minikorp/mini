package com.minikorp.mini.android

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.minikorp.mini.Resource
import kotlin.math.roundToInt

/**
 * Toggle two views between content / loading / error based on [Resource] state.
 *
 * Has no effect when resource is idle.
 */
fun toggleViewsVisibility(
        resource: Resource<*>,
        contentView: View? = null,
        loadingView: View? = null,
        errorView: View? = null,
        idleView: View? = null,
        invisibilityType: Int = View.INVISIBLE
) {
    val newVisibilities = arrayOf(invisibilityType, invisibilityType, invisibilityType, invisibilityType)
    val indexToMakeVisible =
            when {
                resource.isSuccess -> 0
                resource.isLoading -> 1
                resource.isFailure -> 2
                resource.isEmpty -> 3
                else -> throw UnsupportedOperationException()
            }
    newVisibilities[indexToMakeVisible] = View.VISIBLE
    contentView?.visibility = newVisibilities[0]
    loadingView?.visibility = newVisibilities[1]
    errorView?.visibility = newVisibilities[2]
    idleView?.visibility = newVisibilities[3]
}

fun ViewGroup.inflateNoAttach(@LayoutRes layout: Int): View {
    return LayoutInflater.from(this.context).inflate(layout, this, false)
}

fun View.makeVisible() = run { visibility = View.VISIBLE }
fun View.makeInvisible() = run { visibility = View.INVISIBLE }
fun View.makeGone() = run { visibility = View.GONE }

/**
 * 8.dp -> 8dp in value in pixels
 */
val Number.dp: Int get() = (this.toFloat() * Resources.getSystem().displayMetrics.density).roundToInt()
