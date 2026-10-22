package za.co.richfield.smartpantry;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = Ui.page(this, "Settings");
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        CheckBox alerts = new CheckBox(this);
        alerts.setText("Enable expiring-soon reminder preference");
        alerts.setTextSize(16);
        alerts.setChecked(preferences.getBoolean("expiry_alerts", true));
        root.addView(alerts);
        root.addView(Ui.text(this, "This preference is stored locally on the device. Expiry dates can be recorded with pantry items.", 14));
        root.addView(Ui.button(this, "Save settings"));
        root.getChildAt(root.getChildCount() - 1).setOnClickListener(view -> {
            preferences.edit().putBoolean("expiry_alerts", alerts.isChecked()).apply();
            Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
        });
        root.addView(Ui.text(this, "About", 19));
        root.addView(Ui.text(this, "Smart Pantry Manager reduces household food waste by matching recipes against ingredients already at home.", 15));
        Ui.navigation(this, root);
        setContentView(root);
    }
}
