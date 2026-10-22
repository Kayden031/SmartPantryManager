package za.co.richfield.smartpantry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.LinearLayout;

import java.util.List;

import za.co.richfield.smartpantry.recipe.SuggestedRecipesActivity;

public class MainActivity extends Activity {
    private DatabaseHelper database;
    private List<Ingredient> ingredients;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new DatabaseHelper(this);
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        if (database != null) render();
    }

    private void render() {
        LinearLayout root = Ui.page(this, "Smart Pantry Manager");
        root.addView(Ui.text(this, "Track ingredients and discover meals you can make without shopping.", 15));
        root.addView(Ui.button(this, "+ Add ingredient"));
        root.getChildAt(root.getChildCount() - 1).setOnClickListener(view -> startActivity(new Intent(this, IngredientFormActivity.class)));
        root.addView(Ui.button(this, "Find suggested recipes"));
        root.getChildAt(root.getChildCount() - 1).setOnClickListener(view -> startActivity(new Intent(this, SuggestedRecipesActivity.class)));
        root.addView(Ui.text(this, "YOUR PANTRY - tap to edit, long-press to delete", 16));

        ingredients = database.getPantry();
        if (ingredients.isEmpty()) {
            root.addView(Ui.text(this, "Your pantry is empty. Add an ingredient to get started.", 16));
        } else {
            ListView list = new ListView(this);
            list.setAdapter(new PantryAdapter(this, ingredients));
            list.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = new Intent(this, IngredientFormActivity.class);
                intent.putExtra("ingredient_id", ingredients.get(position).id);
                startActivity(intent);
            });
            list.setOnItemLongClickListener((parent, view, position, id) -> {
                Ingredient ingredient = ingredients.get(position);
                new AlertDialog.Builder(this)
                        .setTitle("Delete ingredient")
                        .setMessage("Delete " + ingredient.name + " from your pantry?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (dialog, which) -> { database.deleteIngredient(ingredient.id); render(); })
                        .show();
                return true;
            });
            root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        }
        Ui.navigation(this, root);
        setContentView(root);
    }
}
