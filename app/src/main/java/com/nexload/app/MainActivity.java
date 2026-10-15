
package com.nexload.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(Color.rgb(9, 10, 22));

        TextView title = new TextView(this);
        title.setText("NEXLOAD");
        title.setTextSize(34);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.rgb(0, 210, 255));
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("AI Media Downloader");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.rgb(185, 125, 255));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 12, 0, 50);
        layout.addView(subtitle);

        EditText urlInput = new EditText(this);
        urlInput.setHint("Video veya görsel bağlantısı");
        urlInput.setSingleLine(true);
        urlInput.setTextColor(Color.WHITE);
        urlInput.setHintTextColor(Color.LTGRAY);
        layout.addView(urlInput,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        );

        Button downloadButton = new Button(this);
        downloadButton.setText("İNDİR");
        downloadButton.setTextColor(Color.WHITE);

        GradientDrawable buttonBg = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[] {
                Color.rgb(0, 120, 255),
                Color.rgb(145, 45, 220)
            }
        );
        buttonBg.setCornerRadius(22);
        downloadButton.setBackground(buttonBg);

        LinearLayout.LayoutParams buttonParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                140
            );
        buttonParams.topMargin = 35;
        layout.addView(downloadButton, buttonParams);

        downloadButton.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();

            if (url.isEmpty()) {
                Toast.makeText(this,
                    "Lütfen bağlantı girin",
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                    "İndirme özelliği sonraki aşamada eklenecek",
                    Toast.LENGTH_LONG).show();
            }
        });

        setContentView(layout);
    }
}
