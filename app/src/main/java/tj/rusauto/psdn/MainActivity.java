package tj.rusauto.psdn;

import android.app.Activity;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(9, 19, 39);
    private final int CARD = Color.rgb(20, 37, 65);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(160, 180, 205);
    private final int BLUE = Color.rgb(36, 118, 240);
    private EditText pdsn;
    private TextView factory, master;
    private String factoryValue = "", masterValue = "";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(26), dp(20), dp(30));
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout logoCard = new LinearLayout(this);
        logoCard.setGravity(Gravity.CENTER);
        logoCard.setPadding(dp(18), dp(12), dp(18), dp(12));
        logoCard.setBackground(shape(WHITE, 20));
        ImageView logo = new ImageView(this);
        logo.setImageResource(tj.rusauto.app.R.drawable.rusauto_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logoCard.addView(logo, new LinearLayout.LayoutParams(-1, dp(128)));
        root.addView(logoCard, params(0, 22));

        TextView title = text("PDSN GENERATOR", 23, WHITE, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title, params(0, 6));
        TextView sub = text("Генератор паролей • Offline", 14, MUTED, false);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub, params(0, 24));

        LinearLayout inputCard = card(root);
        inputCard.addView(text("Введите PDSN", 15, WHITE, true), params(0, 12));
        pdsn = new EditText(this);
        pdsn.setSingleLine(true);
        pdsn.setTextSize(17);
        pdsn.setTextColor(WHITE);
        pdsn.setHintTextColor(MUTED);
        pdsn.setHint("PDSN");
        pdsn.setPadding(dp(15), 0, dp(15), 0);
        pdsn.setBackground(shape(Color.rgb(32, 52, 82), 12));
        inputCard.addView(pdsn, new LinearLayout.LayoutParams(-1, dp(54)));

        Button generate = new Button(this);
        generate.setText("GENERATE");
        generate.setTextColor(WHITE);
        generate.setTextSize(16);
        generate.setAllCaps(false);
        generate.setTypeface(null, Typeface.BOLD);
        generate.setBackground(shape(BLUE, 12));
        inputCard.addView(generate, params(18, 0));
        generate.getLayoutParams().height = dp(54);

        LinearLayout resultCard = card(root);
        resultCard.addView(text("РЕЗУЛЬТАТЫ", 15, MUTED, true), params(0, 16));
        resultCard.addView(text("Factory Password", 15, WHITE, true), params(0, 8));
        factory = text("—", 21, WHITE, true);
        factory.setTextIsSelectable(true);
        resultCard.addView(factory, params(0, 8));
        Button copyFactory = copyButton("Копировать Factory");
        resultCard.addView(copyFactory, params(0, 18));
        resultCard.addView(text("Master Password", 15, WHITE, true), params(0, 8));
        master = text("—", 21, WHITE, true);
        master.setTextIsSelectable(true);
        resultCard.addView(master, params(0, 8));
        Button copyMaster = copyButton("Копировать Master");
        resultCard.addView(copyMaster, params(0, 0));

        generate.setOnClickListener(v -> calculate());
        copyFactory.setOnClickListener(v -> copy(factoryValue));
        copyMaster.setOnClickListener(v -> copy(masterValue));
    }

    private LinearLayout card(LinearLayout root) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(18), dp(18), dp(18));
        panel.setBackground(shape(CARD, 18));
        root.addView(panel, params(0, 16));
        return panel;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    private Button copyButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(WHITE);
        b.setBackground(shape(Color.rgb(37, 65, 105), 10));
        return b;
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private LinearLayout.LayoutParams params(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(top);
        p.bottomMargin = dp(bottom);
        return p;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void calculate() {
        String p = pdsn.getText().toString().trim();
        if (p.isEmpty()) {
            pdsn.setError("Введите PDSN");
            return;
        }
        String salt = "E13S31NMP5DfCs24";
        StringBuilder mixed = new StringBuilder();
        for (int i = 0; i < Math.max(p.length(), salt.length()); i++) {
            if (i < p.length()) mixed.append(p.charAt(i));
            if (i < salt.length()) mixed.append(salt.charAt(i));
        }
        try {
            String h1 = sha256(mixed.toString());
            String h2 = sha256(h1);
            factoryValue = "DfCs" + h2.substring(0, 8) + "#!";
            masterValue = "DfCs0715#!";
            factory.setText(factoryValue);
            master.setText(masterValue);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка вычисления", Toast.LENGTH_LONG).show();
        }
    }

    private static String sha256(String s) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) out.append(String.format(Locale.ROOT, "%02x", b & 0xff));
        return out.toString();
    }

    private void copy(String value) {
        if (value.isEmpty()) {
            Toast.makeText(this, "Сначала нажмите GENERATE", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("rusauto.tj", value));
            Toast.makeText(this, "Скопировано", Toast.LENGTH_SHORT).show();
        }
    }
    }
