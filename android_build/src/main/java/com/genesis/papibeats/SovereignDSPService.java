package com.genesis.papibeats;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.media.audiofx.Equalizer;

public class SovereignDSPService extends Service {
    private Equalizer eq;
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            eq = new Equalizer(0, 0);
            eq.setEnabled(true);
            short bands = eq.getNumberOfBands();
            eq.setBandLevel((short)0, (short)1500);
            eq.setBandLevel((short)(bands-1), (short)1500);
        } catch (Exception e) {}
    }
    @Override
    public IBinder onBind(Intent intent) { return null; }
    @Override
    public void onDestroy() {
        if (eq != null) { eq.release(); }
        super.onDestroy();
    }
}
