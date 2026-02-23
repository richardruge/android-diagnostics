package com.diagnostics.android;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private TextView diagnosticTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        diagnosticTextView = findViewById(R.id.diagnosticTextView);
        
        collectDiagnostics();
    }

    private void collectDiagnostics() {
        StringBuilder diagnostics = new StringBuilder();
        
        // Device Information
        diagnostics.append("=== DEVICE INFORMATION ===\n\n");
        diagnostics.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
        diagnostics.append("Model: ").append(Build.MODEL).append("\n");
        diagnostics.append("Device: ").append(Build.DEVICE).append("\n");
        diagnostics.append("Android Version: ").append(Build.VERSION.RELEASE).append("\n");
        diagnostics.append("SDK Level: ").append(Build.VERSION.SDK_INT).append("\n");
        diagnostics.append("Board: ").append(Build.BOARD).append("\n");
        diagnostics.append("Hardware: ").append(Build.HARDWARE).append("\n\n");

        // Battery Information
        diagnostics.append("=== BATTERY INFORMATION ===\n\n");
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = (level / (float) scale) * 100;
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            
            diagnostics.append("Battery Level: ").append(String.format("%.1f", batteryPct)).append("%\n");
            diagnostics.append("Charging: ").append(isCharging ? "Yes" : "No").append("\n");
            diagnostics.append("Temperature: ").append(temperature / 10.0).append("°C\n");
            diagnostics.append("Voltage: ").append(voltage).append(" mV\n\n");
        }

        // Memory Information
        diagnostics.append("=== MEMORY INFORMATION ===\n\n");
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        
        long totalMemory = memoryInfo.totalMem;
        long availableMemory = memoryInfo.availMem;
        long usedMemory = totalMemory - availableMemory;
        
        diagnostics.append("Total RAM: ").append(formatBytes(totalMemory)).append("\n");
        diagnostics.append("Available RAM: ").append(formatBytes(availableMemory)).append("\n");
        diagnostics.append("Used RAM: ").append(formatBytes(usedMemory)).append("\n");
        diagnostics.append("Low Memory: ").append(memoryInfo.lowMemory ? "Yes" : "No").append("\n\n");

        // Storage Information
        diagnostics.append("=== STORAGE INFORMATION ===\n\n");
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long totalStorage = blockSize * totalBlocks;
        long availableStorage = blockSize * availableBlocks;
        long usedStorage = totalStorage - availableStorage;
        
        diagnostics.append("Total Internal: ").append(formatBytes(totalStorage)).append("\n");
        diagnostics.append("Available Internal: ").append(formatBytes(availableStorage)).append("\n");
        diagnostics.append("Used Internal: ").append(formatBytes(usedStorage)).append("\n\n");

        // Network Information
        diagnostics.append("=== NETWORK INFORMATION ===\n\n");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        
        diagnostics.append("Network Connected: ").append(isConnected ? "Yes" : "No").append("\n");
        if (isConnected) {
            diagnostics.append("Network Type: ").append(activeNetwork.getTypeName()).append("\n");
            diagnostics.append("Network Subtype: ").append(activeNetwork.getSubtypeName()).append("\n");
            
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                diagnostics.append("WiFi SSID: ").append(wifiInfo.getSSID()).append("\n");
                diagnostics.append("WiFi Signal: ").append(wifiInfo.getRssi()).append(" dBm\n");
                diagnostics.append("Link Speed: ").append(wifiInfo.getLinkSpeed()).append(" Mbps\n");
            }
        }
        diagnostics.append("\n");

        // Display Information
        diagnostics.append("=== DISPLAY INFORMATION ===\n\n");
        android.util.DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        diagnostics.append("Screen Width: ").append(displayMetrics.widthPixels).append(" px\n");
        diagnostics.append("Screen Height: ").append(displayMetrics.heightPixels).append(" px\n");
        diagnostics.append("Density: ").append(displayMetrics.density).append("\n");
        diagnostics.append("DPI: ").append(displayMetrics.densityDpi).append("\n\n");

        // CPU Information
        diagnostics.append("=== CPU INFORMATION ===\n\n");
        diagnostics.append("CPU ABI: ").append(Build.SUPPORTED_ABIS[0]).append("\n");
        diagnostics.append("Number of Cores: ").append(Runtime.getRuntime().availableProcessors()).append("\n");

        diagnosticTextView.setText(diagnostics.toString());
    }

    private String formatBytes(long bytes) {
        DecimalFormat df = new DecimalFormat("#.##");
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return df.format(bytes / 1024.0) + " KB";
        else if (bytes < 1024 * 1024 * 1024) return df.format(bytes / (1024.0 * 1024.0)) + " MB";
        else return df.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
    }
}
