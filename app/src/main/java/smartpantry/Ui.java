package za.co.richfield.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import za.co.richfield.smartpantry.recipe.SuggestedRecipesActivity;

public final class Ui {
    private static final int GREEN = Color.rgb(46, 125, 50);
    private static final int DARK_GREEN = Color.rgb(27, 94, 32);
    private Ui() { }

    public static LinearLayout page(Activity activity, String title) {
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 28, 32, 20);
        root.setBackgroundColor(Color.rgb(247, 250, 247));
        TextView heading = text(activity, title, 25);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setTextColor(DARK_GREEN);
        heading.setPadding(0, 0, 0, 18);
        root.addView(heading);
        return root;
    }

    public static TextView text(Activity activity, String value, int sizeSp) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(Color.rgb(40, 40, 40));
        view.setPadding(4, 8, 4, 8);
        return view;
    }

    public static Button button(Activity activity, String label) {
        Button button = new Button(activity);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(ColorStateList.valueOf(GREEN));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 6, 0, 6);
        button.setLayoutParams(params);
        return button;
    }

    public static ScrollView scroll(Activity activity, View child) {
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(child);
        return scroll;
    }

    public static void navigation(Activity activity, LinearLayout root) {
        LinearLayout navigation = new LinearLayout(activity);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setGravity(Gravity.CENTER);
        String[] labels = {"Pantry", "Recipes", "Settings"};
        Class<?>[] screens = {MainActivity.class, SuggestedRecipesActivity.class, SettingsActivity.class};
        for (int index = 0; index < labels.length; index++) {
            final Class<?> screen = screens[index];
            Button item = new Button(activity);
            item.setText(labels[index]);
            item.setTextSize(12);
            item.setAllCaps(false);
            item.setOnClickListener(view -> {
                if (activity.getClass().equals(screen)) return;
                Intent intent = new Intent(activity, screen);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            });
            navigation.addView(item, new LinearLayout.LayoutParams(0, -2, 1));
        }
        root.addView(navigation);
    }
}
