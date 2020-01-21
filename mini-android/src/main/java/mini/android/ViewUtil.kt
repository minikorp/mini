package mini.android

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import mini.Resource
import kotlin.math.roundToInt

/**
 * Toggle two views between content / loading / error based on [Resource] state.
 *
 * Has no effect when resource is idle.
 */
fun toggleViewsVisibility(
    resource: Resource<*>,
    contentView: View,
    loadingView: View,
    errorView: View,
    invisibilityType: Int = View.INVISIBLE
) {
    val (content, loading, error) =
        when {
            resource.isSuccess -> Triple(View.VISIBLE, invisibilityType, invisibilityType)
            resource.isLoading -> Triple(invisibilityType, View.VISIBLE, invisibilityType)
            resource.isEmpty   -> Triple(invisibilityType, invisibilityType, View.VISIBLE)
            else               -> return
        }
    contentView.visibility = content
    loadingView.visibility = loading
    errorView.visibility = error
}

fun ViewGroup.inflateNoAttach(@LayoutRes layout: Int): View {
    return LayoutInflater.from(this.context).inflate(layout, this, false)
}

fun View.makeVisible() = run { visibility = View.VISIBLE }
fun View.makeInvisible() = run { visibility = View.INVISIBLE }
fun View.makeGone() = run { visibility = View.GONE }

/** dp -> px */
val Number.dp: Int get() = (this.toFloat() * Resources.getSystem().displayMetrics.density).roundToInt()
