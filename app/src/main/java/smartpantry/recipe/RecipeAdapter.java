package za.co.richfield.smartpantry.recipe;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import za.co.richfield.smartpantry.Ui;

/** Custom ListView adapter used for the strict recipe results. */
public class RecipeAdapter extends BaseAdapter {
    private final Activity activity;
    private final List<Recipe> recipes;

    public RecipeAdapter(Activity activity, List<Recipe> recipes) {
        this.activity = activity;
        this.recipes = recipes;
    }

    @Override public int getCount() { return recipes.size(); }
    @Override public Recipe getItem(int position) { return recipes.get(position); }
    @Override public long getItemId(int position) { return getItem(position).id; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        Recipe recipe = getItem(position);
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(16, 14, 16, 14);
        TextView title = Ui.text(activity, recipe.name, 17);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        row.addView(title);
        row.addView(Ui.text(activity, recipe.ingredients.size() + " required ingredients", 14));
        return row;
    }
}
