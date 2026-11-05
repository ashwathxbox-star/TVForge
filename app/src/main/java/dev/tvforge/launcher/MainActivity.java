package dev.tvforge.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StatFs;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(12, 18, 30);
    private final int panel = Color.rgb(25, 35, 53);
    private final int accent = Color.rgb(75, 155, 255);
    private LinearLayout root;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 32, 48, 36);
        root.setBackgroundColor(bg);
        scroll.setFillViewport(true);
        scroll.addView(root);

        TextView brand = new TextView(this);
        brand.setText("TVForge");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(34);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(brand);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your TV. Your home screen.");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(16);
        root.addView(subtitle);

        TextView clock = new TextView(this);
        clock.setText(new SimpleDateFormat("EEEE, d MMMM  •  h:mm a", Locale.getDefault()).format(new Date()));
        clock.setTextColor(0xFFB9CBE3);
        clock.setTextSize(16);
        clock.setPadding(0, 12, 0, 24);
        root.addView(clock);

        TextView heading = section("Your apps");
        root.addView(heading);

        List<AppEntry> apps = loadLaunchableApps();
        if (apps.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No launchable apps were found.");
            empty.setTextColor(Color.WHITE);
            root.addView(empty);
        } else {
            LinearLayout grid = new LinearLayout(this);
            grid.setOrientation(LinearLayout.VERTICAL);
            for (int i = 0; i < apps.size(); i += 3) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                for (int j = i; j < Math.min(i + 3, apps.size()); j++) {
                    AppEntry entry = apps.get(j);
                    Button button = new Button(this);
                    button.setText(entry.label);
                    button.setAllCaps(false);
                    button.setTextSize(16);
                    button.setTextColor(Color.WHITE);
                    button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel));
                    button.setFocusable(true);
                    button.setOnClickListener(v -> launch(entry));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 100, 1);
                    lp.setMargins(8, 8, 8, 8);
                    row.addView(button, lp);
                }
                grid.addView(row);
            }
            root.addView(grid);
        }

        root.addView(section("Device"));
        status = new TextView(this);
        status.setTextColor(0xFFD4DFEF);
        status.setTextSize(15);
        status.setPadding(0, 8, 0, 12);
        status.setText(deviceSummary());
        root.addView(status);

        Button refresh = actionButton("Refresh device information", v -> status.setText(deviceSummary()));
        root.addView(refresh);

        Button settings = actionButton("Android TV settings", v -> {
            try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
            catch (Exception e) { showMessage("Settings could not be opened on this device."); }
        });
        root.addView(settings);

        setContentView(scroll);
        scroll.setFocusableInTouchMode(true);
        scroll.requestFocus();
    }

    private TextView section(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(Color.WHITE);
        t.setTextSize(22);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, 24, 0, 8);
        return t;
    }

    private Button actionButton(String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent));
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 64);
        lp.setMargins(8, 12, 8, 8);
        b.setLayoutParams(lp);
        return b;
    }

    private List<AppEntry> loadLaunchableApps() {
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<android.content.pm.ResolveInfo> results = pm.queryIntentActivities(query, 0);

        // Include ordinary launcher apps too, where TV firmware exposes them.
        Intent ordinary = new Intent(Intent.ACTION_MAIN);
        ordinary.addCategory(Intent.CATEGORY_LAUNCHER);
        results.addAll(pm.queryIntentActivities(ordinary, 0));

        List<AppEntry> entries = new ArrayList<>();
        for (android.content.pm.ResolveInfo ri : results) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            String label = ri.loadLabel(pm).toString();
            boolean duplicate = false;
            for (AppEntry e : entries) if (e.packageName.equals(pkg)) duplicate = true;
            if (!duplicate) entries.add(new AppEntry(label, pkg));
        }
        Collections.sort(entries, (a, b) -> a.label.compareToIgnoreCase(b.label));
        return entries;
    }

    private void launch(AppEntry entry) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(entry.packageName);
        if (intent == null) {
            showMessage("This app does not expose a standard launch activity.");
            return;
        }
        try { startActivity(intent); }
        catch (Exception e) { showMessage("Could not launch " + entry.label + "."); }
    }

    private String deviceSummary() {
        String model = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        String android = android.os.Build.VERSION.RELEASE + " (API " + android.os.Build.VERSION.SDK_INT + ")";
        String ram = "RAM: information unavailable";
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
            android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            ram = String.format(Locale.getDefault(), "Available RAM: %.0f MB",
                    mi.availMem / (1024.0 * 1024.0));
        } catch (Exception ignored) { }
        String storage = "Storage: information unavailable";
        try {
            StatFs stat = new StatFs(getFilesDir().getAbsolutePath());
            long free = stat.getAvailableBytes();
            long total = stat.getTotalBytes();
            storage = String.format(Locale.getDefault(), "App storage volume: %.1f GB free of %.1f GB",
                    free / 1e9, total / 1e9);
        } catch (Exception ignored) { }
        return "Device: " + model + "\nAndroid: " + android + "\n" + ram + "\n" + storage;
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(this).setTitle("TVForge").setMessage(message)
                .setPositiveButton("OK", null).show();
    }

    private static class AppEntry {
        final String label, packageName;
        AppEntry(String label, String packageName) {
            this.label = label;
            this.packageName = packageName;
        }
    }
}
