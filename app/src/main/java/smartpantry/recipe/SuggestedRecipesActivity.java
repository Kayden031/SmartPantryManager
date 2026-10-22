package za.co.richfield.smartpantry.recipe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import za.co.richfield.smartpantry.DatabaseHelper;
import za.co.richfield.smartpantry.Ui;

public class SuggestedRecipesActivity extends Activity {
    private DatabaseHelper database;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new DatabaseHelper(this);
    }

    @Override protected void onResume() {
        super.onResume();
        if (database != null) render();
    }

    private void render() {
        LinearLayout root = Ui.page(this, "Suggested Recipes");
        root.addView(Ui.text(this, "Only recipes with every required ingredient in sufficient quantity are shown.", 15));
        List<Recipe> recipes = database.getStrictMatches();
        if (recipes.isEmpty()) {
            root.addView(Ui.text(this, "No recipes match your pantry yet - add more ingredients or check quantities.", 16));
        } else {
            ListView list = new ListView(this);
            list.setAdapter(new RecipeAdapter(this, recipes));
            list.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = new Intent(this, RecipeDetailActivity.class);
                intent.putExtra("recipe_id", recipes.get(position).id);
                startActivity(intent);
            });
            root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        }
        root.addView(Ui.button(this, "Back to pantry"));
        root.getChildAt(root.getChildCount() - 1).setOnClickListener(view -> finish());
        Ui.navigation(this, root);
        setContentView(root);
    }
}
