package com.indogaro.net;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class IndogaroDaemonService extends Service {
    private static final String TAG = "Indogaro-JARGO";
    private static final String CHANNEL_ID = "indogaro_daemon_channel";
    private Process golangProcess;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1080, buildNotification("Indogaro Daemon Engine Running..."));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (golangProcess == null) {
            startGolangDaemon();
        }
        return START_STICKY; 
    }

    private void startGolangDaemon() {
        new Thread(() -> {
            try {
                File baseDir = new File(getFilesDir(), "moduls");
                File binFile = new File(baseDir, "bin/indogaro");

                if (!binFile.canExecute()) {
                    binFile.setExecutable(true);
                }

                ProcessBuilder pb = new ProcessBuilder(binFile.getAbsolutePath());
                pb.directory(baseDir); 
                pb.redirectErrorStream(true);

                golangProcess = pb.start();
                Log.i(TAG, "Daemon Golang berhasil diinjeksi ke PID Android.");

                BufferedReader reader = new BufferedReader(new InputStreamReader(golangProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d(TAG, "GO-DAEMON: " + line);
                }

                int exitCode = golangProcess.waitFor();
                Log.e(TAG, "Daemon Golang terhenti dengan Exit Code: " + exitCode);
                
            } catch (Exception e) {
                Log.e(TAG, "Fatal Error saat eksekusi Daemon: ", e);
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        if (golangProcess != null) {
            golangProcess.destroy(); 
            Log.w(TAG, "Mengirimkan sinyal SIGTERM ke daemon...");
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Indogaro Network Proxy")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Indogaro Background Daemon",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Menjaga koneksi proxy TCP/SOCKS5 tetap berjalan tanpa delay.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
