package com.minivac.mini.dagger

import com.minivac.mini.flux.Store

interface StoresComponent {
    fun stores(): Map<Class<*>, Store<*>>
}
