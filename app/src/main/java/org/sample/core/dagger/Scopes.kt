package com.frangsierra.larpy.core.dagger

import javax.inject.Scope

/**
 * Dagger scope for app-related dependencies.
 */
@Scope
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class AppScope

/**
 * Dagger scope for activity-related dependencies.
 */
@Scope
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class ActivityScope