package com.example.test_2

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class Map : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.getInstance().onStart()
    }

    override fun onTerminate() {
        super.onTerminate()
        MapKitFactory.getInstance().onStop()
    }
}