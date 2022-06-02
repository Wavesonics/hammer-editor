package com.darkrockstudios.apps.hammer.android

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class HammerApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        Napier.base(DebugAntilog())
    }
}