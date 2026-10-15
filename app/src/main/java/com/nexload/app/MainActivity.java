package com.nexload.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int cyan = Color.rgb(22, 215, 255);
    private final int purple = Color.rgb(174, 73, 255);
    private final int white = Color.rgb(240, 244, 255);
    private final int muted = Color.rgb(163, 175, 209);
    private LinearLayout root;
    private EditText urlInput;
    private TextView status;
    private TextView recentText;
    private long lastDownloadId = -1;
    private ProgressBar progressBar;
    private TextView progressLabel;
    private LinearLayout progressPanel;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private boolean trackingDownload = false;
    private final Runnable progressPoll = new Runnable() {
        @Override public void run() {
            if (!trackingDownload) return;
            updateDownloadProgress();
            if (trackingDownload) progressHandler.postDelayed(this, 650);
        }
    };

    private int dp(float value) { return (int) (getResources().getDisplayMetrics().density * value + .5f); }
    private GradientDrawable panel(int fill, int stroke, float radius) {
        GradientDrawable bg = new GradientDrawable(); bg.setColor(fill);
        bg.setCornerRadius(dp(radius)); bg.setStroke(dp(1.5f), stroke); return bg;
    }
    private GradientDrawable gradient() {
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(0, 104, 235), Color.rgb(62, 35, 176), Color.rgb(175, 39, 218)});
        bg.setCornerRadius(dp(14)); bg.setStroke(dp(2), cyan); return bg;
    }
    private TextView text(String value, float size, int color, boolean bold) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size);
        view.setTextColor(color); if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return view;
    }
    private LinearLayout column() { LinearLayout v = new LinearLayout(this); v.setOrientation(1); return v; }
    private LinearLayout row() { LinearLayout v = new LinearLayout(this); v.setOrientation(0); v.setGravity(Gravity.CENTER_VERTICAL); return v; }
    private LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(width < 0 ? width : dp(width), height < 0 ? height : dp(height));
    }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(3, 7, 21));
        getWindow().setNavigationBarColor(Color.rgb(3, 7, 21));
        root = column(); root.setBackgroundColor(Color.rgb(3, 7, 21)); setContentView(root);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout content = column(); content.setPadding(dp(14), dp(20), dp(14), dp(16)); scroll.addView(content);
        TextView title = text("NEXL⬇AD", 42, cyan, true); title.setGravity(Gravity.CENTER);
        title.setShadowLayer(dp(13), 0, 0, cyan); content.addView(title, lp(-1, -2));
        TextView subtitle = text("A I   M E D I A   D O W N L O A D E R", 12, muted, true);
        subtitle.setGravity(Gravity.CENTER); content.addView(subtitle, lp(-1, -2));
        TextView slogan = text("H I Z L I     •     K O L A Y     •     Y Ü K S E K   K A L İ T E", 9, muted, false);
        slogan.setGravity(Gravity.CENTER); LinearLayout.LayoutParams sloganParams = lp(-1, -2);
        sloganParams.topMargin = dp(12); sloganParams.bottomMargin = dp(28); content.addView(slogan, sloganParams);
        LinearLayout linkPanel = row(); linkPanel.setPadding(dp(12), dp(10), dp(10), dp(10));
        linkPanel.setBackground(panel(Color.rgb(9, 18, 46), cyan, 16)); content.addView(linkPanel, lp(-1, -2));
        TextView chain = text("🔗", 25, cyan, true); chain.setGravity(Gravity.CENTER); linkPanel.addView(chain, lp(38, 54));
        urlInput = new EditText(this); urlInput.setTextColor(white); urlInput.setHintTextColor(muted);
        urlInput.setHint("Video veya görsel bağlantısını yapıştırın..."); urlInput.setTextSize(13);
        urlInput.setSingleLine(true);
        urlInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setBackgroundColor(Color.TRANSPARENT); linkPanel.addView(urlInput, new LinearLayout.LayoutParams(0, dp(54), 1));
        TextView paste = text("▣", 30, white, true); paste.setGravity(Gravity.CENTER);
        paste.setBackground(panel(Color.rgb(24, 24, 69), purple, 12)); linkPanel.addView(paste, lp(46, 52));
        paste.setOnClickListener(v -> {
            ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (manager != null && manager.hasPrimaryClip()) {
                ClipData clip = manager.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence value = clip.getItemAt(0).coerceToText(this);
                    urlInput.setText(value); urlInput.setSelection(urlInput.length());
                    status.setText("Bağlantı alındı • İndirmeye hazır"); return;
                }
            }
            toast("Panoda bağlantı bulunamadı");
        });
        LinearLayout analysis = row(); analysis.setPadding(dp(18), dp(18), dp(12), dp(18));
        analysis.setBackground(panel(Color.rgb(9, 15, 37), purple, 14));
        LinearLayout.LayoutParams analysisParams = lp(-1, -2); analysisParams.topMargin = dp(18);
        content.addView(analysis, analysisParams);
        TextView radar = text("◎", 49, cyan, false); radar.setGravity(Gravity.CENTER); analysis.addView(radar, lp(80, 70));
        LinearLayout info = column(); info.setPadding(dp(12), 0, 0, 0);
        analysis.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        info.addView(text("Bağlantı yapıştırın", 17, white, true));
        status = text("Doğrudan medya bağlantıları indirilebilir.", 12, muted, false); info.addView(status);
        TextView download = text("⬇   İNDİRMEYİ BAŞLAT", 21, Color.WHITE, true);
        download.setGravity(Gravity.CENTER); download.setBackground(gradient()); download.setElevation(dp(7));
        LinearLayout.LayoutParams downloadParams = lp(-1, 82); downloadParams.topMargin = dp(18);
        content.addView(download, downloadParams);
        download.setOnClickListener(v -> startDownload());
        progressPanel = column();
        progressPanel.setPadding(dp(14), dp(14), dp(14), dp(14));
        progressPanel.setBackground(panel(Color.rgb(9, 18, 46), purple, 14));
        LinearLayout.LayoutParams progressParams = lp(-1, -2);
        progressParams.topMargin = dp(14);
        content.addView(progressPanel, progressParams);
        progressLabel = text("⬇ İndirme bekleniyor • %0", 14, cyan, true);
        progressPanel.addView(progressLabel, lp(-1, -2));
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(cyan));
        progressBar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(38, 46, 82)));
        LinearLayout.LayoutParams barParams = lp(-1, 14);
        barParams.topMargin = dp(10);
        progressPanel.addView(progressBar, barParams);
        progressPanel.setVisibility(android.view.View.GONE);
        LinearLayout features = row(); LinearLayout.LayoutParams featureParams = lp(-1, -2);
        featureParams.topMargin = dp(22); featureParams.bottomMargin = dp(20); content.addView(features, featureParams);
        feature(features, "ϟ", "Hızlı", "Tek bağlantı ile\nhızlı indirme", cyan);
        feature(features, "♢", "Kolay", "Otomatik analiz\nve uygun seçenekler", purple);
        feature(features, "☆", "Yüksek Kalite", "Kaynağın sunduğu\nen iyi kalite", Color.rgb(255, 119, 231));
        LinearLayout recent = column(); recent.setPadding(dp(14), dp(14), dp(14), dp(18));
        recent.setBackground(panel(Color.rgb(8, 16, 41), purple, 14)); content.addView(recent, lp(-1, -2));
        LinearLayout recentHeader = row(); recent.addView(recentHeader, lp(-1, -2));
        TextView recentTitle = text("◴  Son İndirmeler", 18, white, true);
        recentHeader.addView(recentTitle, new LinearLayout.LayoutParams(0, -2, 1));
        TextView seeAll = text("Tümünü Gör  ❯", 12, cyan, false); recentHeader.addView(seeAll);
        seeAll.setOnClickListener(v -> openDownloads());
        recentText = text("Henüz bu oturumda indirme başlatılmadı.", 13, muted, false);
        recentText.setGravity(Gravity.CENTER); LinearLayout.LayoutParams emptyParams = lp(-1, 90);
        emptyParams.topMargin = dp(8); recent.addView(recentText, emptyParams);
        LinearLayout nav = row(); nav.setPadding(dp(5), dp(7), dp(5), dp(7));
        nav.setBackground(panel(Color.rgb(8, 13, 35), cyan, 12)); root.addView(nav, lp(-1, 78));
        navItem(nav, "⌂", "Ana Sayfa", true); navItem(nav, "⬇", "İndirmeler", false);
        navItem(nav, "✧", "AI Araçları", false); navItem(nav, "⚙", "Ayarlar", false);
    }

    private void startDownload() {
        String link = urlInput.getText().toString().trim();
        if (link.isEmpty()) { toast("Lütfen önce bağlantı yapıştırın"); return; }
        Uri uri;
        try { uri = Uri.parse(link); } catch (Exception e) { toast("Geçersiz bağlantı"); return; }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) || uri.getHost() == null) {
            toast("Geçerli bir http/https bağlantısı girin"); return;
        }
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
        String extension = null;
        for (String ext : new String[]{".jpg", ".jpeg", ".png", ".webp", ".gif", ".mp4", ".webm", ".mp3", ".m4a", ".pdf"}) {
            if (path.endsWith(ext)) { extension = ext; break; }
        }
        if (extension == null) {
            status.setText("Bu bağlantı doğrudan dosya adresi değil. Platform analizi henüz desteklenmiyor.");
            toast("Doğrudan dosya bağlantısı gerekli (.jpg, .png, .mp4 vb.)"); return;
        }
        String fileName = "nexload_" + System.currentTimeMillis() + extension;
        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(fileName);
            request.setDescription("NEXLOAD medya indiriliyor");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager == null) { toast("İndirme yöneticisi kullanılamıyor"); return; }
            lastDownloadId = manager.enqueue(request);
            progressHandler.removeCallbacks(progressPoll);
            trackingDownload = true;
            progressPanel.setVisibility(android.view.View.VISIBLE);
            progressBar.setIndeterminate(true);
            progressLabel.setText("⬇ İndirme başlatılıyor...");
            progressHandler.post(progressPoll);
            status.setText("İndirme başlatıldı • Sonucu İndirmeler'den kontrol edin");
            recentText.setText("Başlatıldı: " + fileName + "\nİndirme durumunu bildirimlerden kontrol edin.");
            toast("İndirme kuyruğuna eklendi");
        } catch (Exception e) {
            status.setText("İndirme başlatılamadı: " + e.getClass().getSimpleName());
            toast("İndirme başlatılamadı");
        }
    }

    private void updateDownloadProgress() {
        if (lastDownloadId < 0) { trackingDownload = false; return; }
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null) { trackingDownload = false; return; }
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(lastDownloadId);
        try (Cursor cursor = manager.query(query)) {
            if (cursor == null || !cursor.moveToFirst()) {
                trackingDownload = false;
                progressBar.setIndeterminate(false);
                progressLabel.setText("İndirme kaydı bulunamadı");
                return;
            }
            int state = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            long received = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            long total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            if (state == DownloadManager.STATUS_SUCCESSFUL) {
                trackingDownload = false;
                progressBar.setIndeterminate(false);
                progressBar.setProgress(100);
                progressLabel.setText("✓ İndirme tamamlandı • %100");
                status.setText("Dosya İndirilenler klasörüne kaydedildi");
                recentText.setText("Son indirme tamamlandı • İndirmeler'den açabilirsiniz");
            } else if (state == DownloadManager.STATUS_FAILED) {
                trackingDownload = false;
                progressBar.setIndeterminate(false);
                progressLabel.setText("✕ İndirme başarısız");
                status.setText("Bağlantı veya sunucu nedeniyle indirme başarısız oldu");
                recentText.setText("Son indirme başarısız oldu");
            } else if (total > 0) {
                int percent = (int) Math.min(99, Math.max(0, received * 100.0 / total));
                progressBar.setIndeterminate(false);
                progressBar.setProgress(percent);
                progressLabel.setText(String.format(Locale.getDefault(), "⬇ İndiriliyor • %%%d  (%.1f / %.1f MB)",
                        percent, received / 1048576.0, total / 1048576.0));
            } else {
                progressBar.setIndeterminate(true);
                progressLabel.setText(String.format(Locale.getDefault(), "⬇ İndiriliyor • %.1f MB (toplam boyut bilinmiyor)", received / 1048576.0));
            }
        } catch (Exception e) {
            trackingDownload = false;
            progressBar.setIndeterminate(false);
            progressLabel.setText("İndirme durumu okunamadı");
        }
    }

    @Override protected void onDestroy() {
        trackingDownload = false;
        progressHandler.removeCallbacks(progressPoll);
        super.onDestroy();
    }

    private void openDownloads() {
        try { startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)); }
        catch (Exception e) { toast("İndirmeler ekranı açılamadı"); }
    }
    private void feature(LinearLayout parent, String symbol, String title, String description, int color) {
        LinearLayout item = column(); item.setGravity(Gravity.CENTER);
        parent.addView(item, new LinearLayout.LayoutParams(0, -2, 1));
        TextView icon = text(symbol, 31, color, true); icon.setGravity(Gravity.CENTER);
        icon.setShadowLayer(dp(9), 0, 0, color); item.addView(icon);
        TextView name = text(title, 13, white, true); name.setGravity(Gravity.CENTER); item.addView(name);
        TextView detail = text(description, 10, muted, false); detail.setGravity(Gravity.CENTER); item.addView(detail);
    }
    private void navItem(LinearLayout parent, String symbol, String label, boolean selected) {
        LinearLayout item = column(); item.setGravity(Gravity.CENTER);
        if (selected) item.setBackground(panel(Color.rgb(7, 36, 76), cyan, 10));
        parent.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
        TextView icon = text(symbol, 27, selected ? cyan : white, true); icon.setGravity(Gravity.CENTER); item.addView(icon);
        TextView name = text(label, 11, selected ? cyan : muted, selected);
        name.setGravity(Gravity.CENTER); item.addView(name);
        item.setOnClickListener(v -> {
            if (label.equals("İndirmeler")) openDownloads();
            else if (!selected) toast(label + " bölümü sonraki aşamada eklenecek");
        });
    }
}
