package com.minivac.mini.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.view.View
import android.view.ViewGroup
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import kotlin.reflect.KClass


fun <T : Activity> testActivity(clazz: KClass<T>,
                                createIntent: ((context: Context) -> Intent)? = null): ActivityTestRule<T> {
    //Overriding the rule throws an exception
    if (createIntent == null) return ActivityTestRule<T>(clazz.java)
    return object : ActivityTestRule<T>(clazz.java) {
        override fun getActivityIntent(): Intent = createIntent(InstrumentationRegistry.getTargetContext())
    }
}


inline fun <reified T : Activity> testActivity(intent: Intent): ActivityTestRule<T> =
        ActivityTestRule(T::class.java)

fun nthChildOf(parentMatcher: Matcher<View>, childPosition: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("with $childPosition child select of type parentMatcher")
        }

        override fun matchesSafely(view: View): Boolean {
            if (view.parent !is ViewGroup) {
                return parentMatcher.matches(view.parent)
            }

            val group = view.parent as ViewGroup
            return parentMatcher.matches(view.parent) && group.getChildAt(childPosition) == view
        }
    }
}