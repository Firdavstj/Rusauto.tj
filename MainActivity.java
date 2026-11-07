package tj.rusauto.psdn;

import android.app.Activity;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MainActivity extends Activity {
    private EditText pdsn, radio;
    private TextView factory, su, master;
    private String factoryValue = "", suValue = "", masterValue = "";
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 40, 28, 24);
        layout.setBackgroundColor(Color.rgb(248,249,252));
        ScrollView scroll = new ScrollView(this); scroll.addView(layout); setContentView(scroll);
        TextView heading = new TextView(this); heading.setText("rusauto.tj"); heading.setTextSize(30); heading.setTextColor(Color.rgb(23,55,99)); layout.addView(heading);
        TextView subtitle = new TextView(this); subtitle.setText("PDSN Generation • Offline"); subtitle.setTextSize(15); layout.addView(subtitle);
        pdsn = new EditText(this); pdsn.setSingleLine(true); pdsn.setHint("Введите PDSN"); layout.addView(pdsn);
        radio = new EditText(this); radio.setSingleLine(true); radio.setHint("Введите persist.radio.sn"); layout.addView(radio);
        Button generate = new Button(this); generate.setText("GENERATE"); layout.addView(generate);
        factory = output(layout, "Factory Password");
        Button copyFactory = button(layout, "Копировать Factory"); copyFactory.setOnClickListener(v -> copy(factoryValue));
        su = output(layout, "SU (raw string)");
        Button copySu = button(layout, "Копировать SU"); copySu.setOnClickListener(v -> copy(suValue));
        master = output(layout, "Master (backup)");
        Button copyMaster = button(layout, "Копировать Master"); copyMaster.setOnClickListener(v -> copy(masterValue));
        generate.setOnClickListener(v -> calculate());
    }
    private TextView output(LinearLayout parent, String name) {
        TextView label = new TextView(this); label.setText(name); label.setTextSize(16); label.setPadding(0,22,0,4); parent.addView(label);
        TextView value = new TextView(this); value.setText("—"); value.setTextSize(20); value.setTextColor(Color.rgb(15,65,140)); value.setTextIsSelectable(true); parent.addView(value); return value;
    }
    private Button button(LinearLayout parent, String title) { Button b = new Button(this); b.setText(title); parent.addView(b); return b; }
    private void calculate() {
        String p = pdsn.getText().toString(); String sn = radio.getText().toString();
        if (p.isEmpty()) { pdsn.setError("Введите PDSN"); return; }
        if (sn.length() < 4) { radio.setError("Нужно минимум 4 символа"); return; }
        String salt = "E13S31NMP5DfCs24";
        StringBuilder mixed = new StringBuilder();
        for (int i=0; i<Math.max(p.length(),salt.length());i++) {
            if (i<p.length()) mixed.append(p.charAt(i));
            if (i<salt.length()) mixed.append(salt.charAt(i));
        }
        try {
            String h1 = sha256(mixed.toString());
            String h2 = sha256(h1);
            factoryValue = "DfCs" + h2.substring(0,8) + "#!";
            suValue = "DfCs" + sn.substring(sn.length()-4) + "#!";
            masterValue = "DfCs0715#!";
            factory.setText(factoryValue); su.setText(suValue); master.setText(masterValue);
        } catch(Exception e) { Toast.makeText(this,"Ошибка вычисления",Toast.LENGTH_LONG).show(); }
    }
    private static String sha256(String s) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        for (byte b:bytes) out.append(String.format(java.util.Locale.ROOT,"%02x", b & 0xff));
        return out.toString();
    }
    private void copy(String value) {
        if (value.isEmpty()) { Toast.makeText(this,"Сначала нажмите GENERATE",Toast.LENGTH_SHORT).show(); return; }
        ((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("rusauto.tj",value));
        Toast.makeText(this,"Скопировано",Toast.LENGTH_SHORT).show();    }
}
