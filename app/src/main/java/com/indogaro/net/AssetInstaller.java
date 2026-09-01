package com.indogaro.net;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetInstaller {
    private static final String TAG = "Indogaro-JARGO";

    public static void initializeEnvironment(Context context) throws IOException {
        File baseDir = new File(context.getFilesDir(), "moduls");
        AssetManager assetManager = context.getAssets();

        Log.i(TAG, "Memulai ekstraksi Core Engine ke: " + baseDir.getAbsolutePath());

        // 1. Ekstrak dan aktifkan Binary Golang
        File binDir = new File(baseDir, "bin");
        if (!binDir.exists()) binDir.mkdirs();
        File binFile = new File(binDir, "indogaro");
        
        copyAssetFile(assetManager, "indogaro", binFile.getAbsolutePath());
        if (!binFile.canExecute()) {
            binFile.setExecutable(true, false);
            Log.i(TAG, "Apply +x unix permission ke binary indogaro.");
        }

        // 2. Ekstrak folder config/ (.env, brain.dat, state.json)
        File configDir = new File(baseDir, "config");
        copyAssetFolder(assetManager, "config", configDir.getAbsolutePath());

        // 3. Ekstrak folder assets/ (blocklists beserta seluruh isi .txt)
        File assetsDir = new File(baseDir, "assets");
        copyAssetFolder(assetManager, "assets", assetsDir.getAbsolutePath());

        Log.i(TAG, "Deployment seluruh file konfigurasi dan blocklist berhasil.");
    }

    private static void copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            if (files == null || files.length == 0) {
                copyAssetFile(assetManager, fromAssetPath, toPath);
            } else {
                new File(toPath).mkdirs();
                for (String file : files) {
                    if (file.equals("images") || file.equals("webkit")) continue;
                    
                    String nextAssetPath = fromAssetPath.isEmpty() ? file : fromAssetPath + "/" + file;
                    copyAssetFolder(assetManager, nextAssetPath, toPath + "/" + file);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Gagal mengekstrak folder: " + fromAssetPath, e);
        }
    }

    private static void copyAssetFile(AssetManager assetManager, String fromAssetPath, String toPath) {
        try (InputStream in = assetManager.open(fromAssetPath);
             OutputStream out = new FileOutputStream(toPath)) {
            byte[] buffer = new byte[16384];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            // Abaikan jika direktori terdeteksi sebagai file
        }
    }
}
