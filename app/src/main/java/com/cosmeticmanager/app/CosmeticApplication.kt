package com.cosmeticmanager.app

import android.app.Application
import com.cosmeticmanager.app.data.ServiceLocator

/**
 * Application entry point. Runs once before any activity and initializes the [ServiceLocator] with
 * the application [android.content.Context], which it needs to open the Room database.
 *
 * Registered in the manifest via android:name=".CosmeticApplication".
 */
class CosmeticApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initialize(this)
    }
}
