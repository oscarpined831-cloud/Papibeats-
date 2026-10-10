package com.genesis.papibeats

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.media.audiofx.Equalizer
import android.util.Log

class SovereignDSPService : Service() {
    private var eq: Equalizer? = null

    override fun onCreate() {
        super.onCreate()
        try {
            eq = Equalizer(0, 0)
            eq?.enabled = true
            val bands = eq?.numberOfBands ?: 0
            if (bands > 0) {
                eq?.setBandLevel(0, 1500) // Bass boost (+15dB)
                eq?.setBandLevel((bands - 1).toShort(), 1500) // Treble boost
            }
            Log.i("SovereignDSP", "SovereignDSPService initialized successfully.")
        } catch (e: Exception) {
            Log.e("SovereignDSP", "Failed to initialize Equalizer: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            eq?.release()
        } catch (e: Exception) {
            Log.e("SovereignDSP", "Error releasing equalizer: ${e.message}")
        }
        super.onDestroy()
    }
}
