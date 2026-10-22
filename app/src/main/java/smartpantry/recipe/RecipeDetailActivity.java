package za.co.richfield.smartpantry.recipe;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import za.co.richfield.smartpantry.DatabaseHelper;
import za.co.richfield.smartpantry.Ui;

public class RecipeDetailActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHelper database = new DatabaseHelper(this);
        long recipeId = getIntent().getLongExtra("recipe_id", -1);
        Recipe selected = null;
        for (Recipe recipe : database.getRecipes()) if (recipe.id == recipeId) { selected = recipe; break; }
        LinearLayout root = Ui.page(this, selected == null ? "Recipe not found" : selected.name);
        if (selected != null) {
            root.addView(Ui.text(this, "Required ingredients", 19));
            for (Recipe.RecipeIngredient ingredient : selected.ingredients) {
                root.addView(Ui.text(this, "- " + ingredient.name + ": " + ingredient.quantity + " " + ingredient.unit, 16));
            }
            root.addView(Ui.text(this, "Preparation method", 19));
            root.addView(Ui.text(this, selected.method.replace(". ", ".\n"), 16));
        }
        root.addView(Ui.button(this, "Back to recipes"));
        root.getChildAt(root.getChildCount() - 1).setOnClickListener(view -> finish());
        setContentView(Ui.scroll(this, root));
    }
}
