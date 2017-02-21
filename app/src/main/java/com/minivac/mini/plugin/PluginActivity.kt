package com.minivac.mini.plugin

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.MotionEvent
import timber.log.Timber
import java.util.*
import javax.inject.Inject


private const val TAG = "PluginActivity"
private const val LC_TAG = "LifeCycle"
private const val ARG_PLUGIN_SAVED_STATES = "pluginStates"

abstract class PluginActivity : AppCompatActivity() {

    // Plugins
    private lateinit var pluginMap: Map<Class<*>, Plugin>
    private val pluginList = ArrayList<Plugin>()
    private val pluginBackList = ArrayList<Plugin>()
    private val pluginTouchList = ArrayList<Plugin>()

    // Reused events
    private val sharedTouchCallback: WrappedCallback<MotionEvent, Boolean>
    private val sharedKeyDownCallback: WrappedCallback<KeyEvent, Boolean>
    private val sharedKeyUpCallback: WrappedCallback<KeyEvent, Boolean>
    private val sharedBackEvent: WrappedCallback<Nothing?, Nothing?>

    init {
        val dummyMotionEvent = MotionEvent.obtain(0, 0, 0, 0.0f, 0.0f, 0)
        val dummyKeyEvent = KeyEvent(0, 0)
        //val DUMMY_KEY_EVENT = KeyEvent

        sharedTouchCallback = object : WrappedCallback<MotionEvent, Boolean>(dummyMotionEvent, false) {
            override fun call() {
                onTouchEvent(parameters)
            }
        }

        sharedKeyDownCallback = object : WrappedCallback<KeyEvent, Boolean>(dummyKeyEvent, false) {
            override fun call() {
                onKeyDown(parameters.keyCode, parameters)
            }
        }

        sharedKeyUpCallback = object : WrappedCallback<KeyEvent, Boolean>(dummyKeyEvent, false) {
            override fun call() {
                onKeyUp(parameters.keyCode, parameters)
            }
        }

        sharedBackEvent = object : WrappedCallback<Nothing?, Nothing?>(null, null) {
            override fun call() {
                onBackPressed()
            }
        }

        dummyMotionEvent.recycle()
    }

    abstract fun createPluginMap(): Map<Class<*>, Plugin>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val now = System.currentTimeMillis()

        pluginMap = createPluginMap()

        //Register lifecycle, back and touch
        pluginList.addAll(pluginMap.values)
        pluginBackList.addAll(pluginMap.values)
        pluginList.filterTo(pluginTouchList) { it.properties.willHandleTouch }

        //Sort by priority
        Collections.sort(pluginList) { o1, o2 ->
            Integer.compare(o1.properties.lifecyclePriority, o2.properties.lifecyclePriority) }
        Collections.sort(pluginBackList) { o1, o2 ->
            Integer.compare(o1.properties.backPriority, o2.properties.backPriority) }
        Collections.sort(pluginTouchList) { o1, o2 ->
            Integer.compare(o1.properties.touchPriority, o2.properties.touchPriority) }

        //Restore plugin states
        var pluginSavedStates: ArrayList<Bundle>? = null
        if (savedInstanceState != null) {
            pluginSavedStates = savedInstanceState.getParcelableArrayList<Bundle>(ARG_PLUGIN_SAVED_STATES)
        }


        val pluginCount = pluginList.size
        val loadTimes = LongArray(pluginCount)

        Timber.tag(LC_TAG).d("onCreate")
        for (i in 0..pluginCount - 1) {
            var state: Bundle? = null
            if (pluginSavedStates != null) state = pluginSavedStates[i]
            loadTimes[i] = System.nanoTime()
            pluginList[i].onCreate(state)
            loadTimes[i] = System.nanoTime() - loadTimes[i] //Elapsed
        }

        Timber.tag(LC_TAG).d("onCreateDynamicView")
        for (i in 0..pluginCount - 1) {
            pluginList[i].onCreateDynamicView()
        }

        val elapsed = System.currentTimeMillis() - now
        Timber.tag(LC_TAG).d("┌ Activity with %2d plugins loaded in %3d ms", pluginCount, elapsed)
        Timber.tag(LC_TAG).d("├──────────────────────────────────────────")
        for (i in 0..pluginCount - 1) {
            val plugin = pluginList[i]
            var boxChar = "├"
            if (plugin === pluginList[pluginCount - 1]) {
                boxChar = "└"
            }
            Timber.tag(LC_TAG).d("%s %s - %d ms",
                    boxChar,
                    plugin.javaClass.simpleName,
                    loadTimes[i] / 10000000)
        }
    }

    ////////////////////////////////////////////////////////
    // Life-Cycle
    ////////////////////////////////////////////////////////

    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Timber.tag(LC_TAG).d("onPostCreate")
        for (plugin in pluginList) {
            plugin.onPostCreate()
        }
        for (plugin in pluginList) {
            plugin.onPluginsCreated()
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.tag(LC_TAG).d("onStart")
        for (plugin in pluginList) {
            plugin.onStart()
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.tag(LC_TAG).d("onResume")
        for (plugin in pluginList) {
            plugin.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.tag(LC_TAG).d("onPause")
        for (plugin in pluginList) {
            plugin.onPause()
        }
    }

    override fun onStop() {
        super.onStop()
        Timber.tag(LC_TAG).d("onStop")
        for (plugin in pluginList) {
            plugin.onStop()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.tag(LC_TAG).d("onSaveInstanceState")
        val states = ArrayList<Bundle>(pluginList.size)
        for (plugin in pluginList) {
            val pluginBundle = Bundle()
            plugin.onSaveInstanceState(pluginBundle)
            states.add(pluginBundle)
        }
        outState.putParcelableArrayList(ARG_PLUGIN_SAVED_STATES, states)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(LC_TAG).d("onDestroy")
        for (plugin in pluginList) {
            plugin.onDestroy()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Timber.tag(LC_TAG).d("onDestroyDynamicView")
        for (plugin in pluginList) {
            plugin.onDestroyDynamicView()
        }

        Timber.tag(LC_TAG).d("onConfigurationChanged")
        for (plugin in pluginList) {
            plugin.onConfigurationChanged(newConfig)
        }

        Timber.tag(LC_TAG).d("onCreateDynamicView")
        for (plugin in pluginList) {
            plugin.onCreateDynamicView()
        }
    }

    ////////////////////////////////////////////////////////
    // Hardware keys and touch
    ////////////////////////////////////////////////////////

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        Timber.tag(TAG).d("onKeyUp [%s]", event)
        sharedKeyUpCallback.set(event, false)
        for (plugin in pluginList) {
            plugin.onKeyUp(sharedKeyUpCallback)
        }

        if (sharedKeyUpCallback.consumed) return sharedKeyUpCallback.returnValue
        else return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Timber.tag(TAG).d("onKeyDown [%s]", event)
        sharedKeyDownCallback.set(event, false)
        for (plugin in pluginList) {
            plugin.onKeyDown(sharedKeyDownCallback)
        }
        if (sharedKeyDownCallback.consumed) return sharedKeyDownCallback.returnValue
        else return super.onKeyDown(keyCode, event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        sharedTouchCallback.set(ev, false)
        for (plugin in pluginTouchList) {
            plugin.onDispatchTouchEvent(sharedTouchCallback)
        }
        if (sharedTouchCallback.consumed) return sharedTouchCallback.returnValue
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        Timber.tag(TAG).d("onBackPressed")
        sharedBackEvent.set(null, null)
        for (plugin in pluginBackList) {
            plugin.onBackPressed(sharedBackEvent)
        }
        if (!sharedBackEvent.consumed) super.onBackPressed()
    }
}
