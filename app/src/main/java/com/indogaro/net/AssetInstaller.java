package com.indogaro.net;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetInstaller {
    private static final String TAG = "Indogaro-JARGO";

    public static void initializeEnvironment(Context context) throws IOException {
        File baseDir = new File(context.getFilesDir(), "moduls");
        File binDir = new File(baseDir, "bin");
        File configDir = new File(baseDir, "config");
        File assetsDir = new File(baseDir, "assets/blocklists");

        if (!binDir.exists()) binDir.mkdirs();
        if (!configDir.exists()) configDir.mkdirs();
        if (!assetsDir.exists()) assetsDir.mkdirs();

        File binFile = new File(binDir, "indogaro");
        if (!binFile.exists()) {
            Log.i(TAG, "Mengekstrak binary Golang dari APK...");
            copyAsset(context, "indogaro", binFile);
        }

        if (!binFile.canExecute()) {
            boolean success = binFile.setExecutable(true, false);
            Log.i(TAG, "Apply +x unix permission: " + success);
        }

        File envFile = new File(configDir, ".env");
        if (!envFile.exists()) {
            Log.i(TAG, "Membuat file konfigurasi .env bawaan...");
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("# =====================================\n");
                writer.write("# INDOGARO DAEMON CONFIGURATION\n");
                writer.write("# =====================================\n");
                writer.write("APP_ENV=production\n");
                writer.write("DAEMON_PORT=1080\n");
                writer.write("LOG_LEVEL=info\n");
                writer.write("\n# Tuning Memory Pool\n");
                writer.write("POOL_REGULAR_KB=32\n");
                writer.write("POOL_VVIP_MB=4\n");
                writer.write("TCP_NO_DELAY=true\n");
            }
        }

        File stateFile = new File(configDir, "state.json");
        if (!stateFile.exists()) {
            try (FileWriter writer = new FileWriter(stateFile)) {
                writer.write("{\n");
                writer.write("  \"daemon_status\": \"initialized\",\n");
                writer.write("  \"last_restart\": 0,\n");
                writer.write("  \"active_connections\": 0\n");
                writer.write("}\n");
            }
        }

        File filterFile = new File(assetsDir, "filter.txt");
        if (!filterFile.exists()) {
            try (FileWriter writer = new FileWriter(filterFile)) {
                writer.write("# =====================================\n");
                writer.write("# INDOGARO RULE SANITIZER TARGETS\n");
                writer.write("# =====================================\n");
                writer.write("0.0.0.0 malicious-tracker.com\n");
                writer.write("0.0.0.0 ads.tiktok.com\n");
                writer.write("||analytics.google.com^\n");
                writer.write("@@||api.layananindogaro.com^\n");
                writer.write("! Pengecualian internal\n");
            }
        }

        Log.i(TAG, "Lingkungan Daemon Golang berhasil di-setup di: " + baseDir.getAbsolutePath());
    }

    private static void copyAsset(Context context, String assetName, File outFile) throws IOException {
        try (InputStream in = context.getAssets().open(assetName);
             OutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
