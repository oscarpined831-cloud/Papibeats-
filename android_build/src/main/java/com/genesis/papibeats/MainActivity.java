package com.genesis.papibeats;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Color;
import android.Manifest;
import android.content.pm.PackageManager;
import android.view.Gravity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("PAPIBEATS ⚡ SPOTIFY\n[ ANILLO 0 ACTIVADO ]\n\nBASS BOOST GLOBAL Y SECUESTRO DE SESIÓN 0 OPERATIVO.");
        tv.setTextColor(Color.parseColor("#1db954"));
        tv.setBackgroundColor(Color.parseColor("#0a0a0a"));
        tv.setTextSize(18f);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(40, 40, 40, 40);
        setContentView(tv);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS}, 1);
        } else {
            startService(new Intent(this, SovereignDSPService.class));
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, SovereignDSPService.class));
        }
    }
}
