package com.itinerarium

import android.app.Application
import org.maplibre.android.MapLibre

class ItinerariumApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            MapLibre.getInstance(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        lateinit var instance: ItinerariumApplication
            private set
    }
}
