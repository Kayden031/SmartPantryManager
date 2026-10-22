package za.co.richfield.smartpantry.recipe;

import java.util.List;

public class Recipe {
    public final long id;
    public final String name;
    public final String method;
    public final List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String method, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.method = method;
        this.ingredients = ingredients;
    }

    public static class RecipeIngredient {
        public final String name;
        public final double quantity;
        public final String unit;

        public RecipeIngredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }
}
