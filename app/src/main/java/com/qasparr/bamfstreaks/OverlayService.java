package com.qasparr.bamfstreaks;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Floating BAMF button that hovers over the game.
 * Tap to expand the streak pad; tap a streak to fire the BAMF call.
 * Touches nothing inside the game itself.
 */
public class OverlayService extends Service {
    static final String TAG = "BamfOverlay";
    static final String ACTION_STOP = "com.qasparr.bamfstreaks.STOP";
    static final int NOTIF_ID = 77;

    static final String[][] CLIPS = {
            {"FIRST BLOOD", "first_blood.mp3"},
            {"DOUBLE KILL", "double_kill.mp3"},
            {"TRIPLE KILL", "triple_kill.mp3"},
            {"MANIAC", "quad_kill.mp3"},
            {"SAVAGE", "5_kills.mp3"},
            {"SHUT DOWN", "smackdown.mp3"},
            {"LEGENDARY", "immortal.mp3"},
            {"MASSACRE", "massacre.mp3"},
            {"GENOCIDE", "genocide.mp3"},
            {"VICTORY", "victory.mp3"},
    };

    WindowManager wm;
    View bubble;
    LinearLayout panel;
    WindowManager.LayoutParams bubbleParams;
    WindowManager.LayoutParams panelParams;
    SoundPool pool;
    final Map<String, Integer> soundIds = new HashMap<>();
    boolean expanded = false;

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (wm != null) return START_STICKY;
        startForeground(NOTIF_ID, buildNotification());
        try {
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            pool = new SoundPool.Builder().setMaxStreams(3).setAudioAttributes(aa).build();
            for (String[] c : CLIPS) {
                AssetFileDescriptor afd = getAssets().openFd(c[1]);
                soundIds.put(c[1], pool.load(afd, 1));
            }
            wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            buildBubble();
            buildPanel();
            wm.addView(bubble, bubbleParams);
            Log.i(TAG, "overlay up");
        } catch (Exception e) {
            Log.e(TAG, "overlay failed", e);
            stopSelf();
        }
        return START_STICKY;
    }

    int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density); }

    void buildBubble() {
        Button b = new Button(this);
        b.setText("BAMF");
        b.setTextSize(13);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.parseColor("#c9a227"));
        bubble = b;
        bubbleParams = new WindowManager.LayoutParams(
                dp(72), dp(72),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = dp(16);
        bubbleParams.y = dp(220);
        final int[] down = new int[2];
        final int[] start = new int[2];
        b.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    down[0] = (int) e.getRawX(); down[1] = (int) e.getRawY();
                    start[0] = bubbleParams.x; start[1] = bubbleParams.y;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    bubbleParams.x = start[0] + (int) e.getRawX() - down[0];
                    bubbleParams.y = start[1] + (int) e.getRawY() - down[1];
                    wm.updateViewLayout(bubble, bubbleParams);
                    return true;
                case MotionEvent.ACTION_UP:
                    int dx = (int) e.getRawX() - down[0];
                    int dy = (int) e.getRawY() - down[1];
                    if (dx * dx + dy * dy < dp(12) * dp(12)) togglePanel();
                    return true;
            }
            return false;
        });
    }

    void buildPanel() {
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(Color.parseColor("#DD111118"));
        panel.setPadding(dp(8), dp(8), dp(8), dp(8));
        for (String[] c : CLIPS) {
            Button b = new Button(this);
            b.setText(c[0]);
            b.setTextSize(14);
            b.setTextColor(Color.WHITE);
            b.setBackgroundColor(Color.parseColor("#2b2b38"));
            b.setPadding(0, dp(10), 0, dp(10));
            final String asset = c[1];
            b.setOnClickListener(v -> play(asset));
            panel.addView(b);
        }
        Button hide = new Button(this);
        hide.setText("HIDE");
        hide.setTextColor(Color.parseColor("#ff8a8a"));
        hide.setBackgroundColor(Color.parseColor("#3a0d0d"));
        hide.setOnClickListener(v -> togglePanel());
        panel.addView(hide);

        panelParams = new WindowManager.LayoutParams(
                dp(200), WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        panelParams.gravity = Gravity.TOP | Gravity.START;
        panelParams.x = dp(100);
        panelParams.y = dp(120);
    }

    void togglePanel() {
        expanded = !expanded;
        try {
            if (expanded) wm.addView(panel, panelParams);
            else wm.removeView(panel);
        } catch (Exception e) {
            Log.w(TAG, "panel toggle", e);
        }
    }

    void play(String asset) {
        Integer id = soundIds.get(asset);
        if (id != null) pool.play(id, 1f, 1f, 1, 0, 1f);
    }

    Notification buildNotification() {
        String ch = "bamf";
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel(ch, "BAMF Streaks",
                    NotificationManager.IMPORTANCE_LOW));
        }
        Intent stop = new Intent(this, OverlayService.class).setAction(ACTION_STOP);
        PendingIntent pi = PendingIntent.getService(this, 0, stop,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, ch)
                .setContentTitle("BAMF overlay active")
                .setContentText("Tap the BAMF bubble over your game — STOP removes it")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .addAction(new Notification.Action.Builder(null, "STOP", pi).build())
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        try {
            if (pool != null) pool.release();
            if (wm != null) {
                if (bubble != null) wm.removeView(bubble);
                if (panel != null && expanded) wm.removeView(panel);
            }
        } catch (Exception e) {
            Log.w(TAG, "destroy", e);
        }
        wm = null;
        super.onDestroy();
    }
}
