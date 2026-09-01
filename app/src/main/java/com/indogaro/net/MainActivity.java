package com.indogaro.net;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Indogaro-JARGO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            try {
                AssetInstaller.initializeEnvironment(this);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Core Engine terpasang siap dijalankan.", Toast.LENGTH_SHORT).show();
                    startDaemonService();
                });
            } catch (Exception e) {
                Log.e(TAG, "Gagal menginisialisasi lingkungan file:", e);
                runOnUiThread(() -> Toast.makeText(this, "Fatal Error Instalasi", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void startDaemonService() {
        Intent serviceIntent = new Intent(this, IndogaroDaemonService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}
