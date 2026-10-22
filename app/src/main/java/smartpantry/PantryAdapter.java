package za.co.richfield.smartpantry;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** Custom ListView adapter that binds persisted pantry data to a two-line row. */
public class PantryAdapter extends BaseAdapter {
    private final Activity activity;
    private final List<Ingredient> ingredients;

    public PantryAdapter(Activity activity, List<Ingredient> ingredients) {
        this.activity = activity;
        this.ingredients = ingredients;
    }

    @Override public int getCount() { return ingredients.size(); }
    @Override public Ingredient getItem(int position) { return ingredients.get(position); }
    @Override public long getItemId(int position) { return getItem(position).id; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        Ingredient item = getItem(position);
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(16, 14, 16, 14);
        TextView title = Ui.text(activity, item.name + " - " + item.quantity + " " + item.unit, 17);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        row.addView(title);
        if (!item.expiryDate.isEmpty()) row.addView(Ui.text(activity, "Expiry: " + item.expiryDate, 14));
        return row;
    }
}

