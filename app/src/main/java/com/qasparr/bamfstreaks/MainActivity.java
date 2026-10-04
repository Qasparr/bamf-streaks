package com.qasparr.bamfstreaks;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Launcher: grants overlay permission, starts/stops the floating BAMF pad. */
public class MainActivity extends Activity {
    static final int REQ_OVERLAY = 2001;
    TextView status;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 60, 36, 36);
        root.setBackgroundColor(Color.parseColor("#0d0d12"));
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("BAMF STREAKS");
        title.setTextSize(26);
        title.setTextColor(Color.parseColor("#c9a227"));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("A floating BAMF button hovers over Mobile Legends. " +
                "Tap it mid-game to open the streak pad, then tap your streak — " +
                "the BAMF announcer calls it. Nothing in the game is touched.");
        sub.setTextColor(Color.parseColor("#8a8a96"));
        sub.setTextSize(14);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 12, 0, 28);
        root.addView(sub);

        status = new TextView(this);
        status.setText("Status: idle");
        status.setTextColor(Color.WHITE);
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, 24);
        root.addView(status);

        Button start = new Button(this);
        start.setText("SHOW BAMF BUTTON");
        start.setTextSize(17);
        start.setTextColor(Color.parseColor("#111111"));
        start.setBackgroundColor(Color.parseColor("#c9a227"));
        start.setPadding(0, 30, 0, 30);
        start.setOnClickListener(v -> ensureOverlayAndStart());
        root.addView(start,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        Button stop = new Button(this);
        stop.setText("REMOVE");
        stop.setTextSize(15);
        stop.setTextColor(Color.parseColor("#ff8a8a"));
        stop.setBackgroundColor(Color.parseColor("#3a0d0d"));
        stop.setPadding(0, 24, 0, 24);
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, OverlayService.class));
            status.setText("Status: idle");
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 18;
        root.addView(stop, lp);

        setContentView(root);
    }

    void ensureOverlayAndStart() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Allow \"display over other apps\", then tap again",
                    Toast.LENGTH_LONG).show();
            startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())),
                    REQ_OVERLAY);
            return;
        }
        startOverlay();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_OVERLAY && Settings.canDrawOverlays(this)) startOverlay();
    }

    void startOverlay() {
        Intent svc = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc);
        else startService(svc);
        status.setText("Status: BAMF button is live — switch to MLBB");
    }
}
