package za.co.richfield.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import za.co.richfield.smartpantry.recipe.Recipe;

/** Stores the user's pantry and the preloaded recipe catalogue on-device. */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry_date TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, method TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL)");
        seedRecipes(db);
        seedPantry(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    public long addIngredient(String name, double quantity, String unit, String expiryDate) {
        return getWritableDatabase().insert("pantry", null, ingredientValues(name, quantity, unit, expiryDate));
    }

    public int updateIngredient(long id, String name, double quantity, String unit, String expiryDate) {
        return getWritableDatabase().update("pantry", ingredientValues(name, quantity, unit, expiryDate), "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteIngredient(long id) {
        return getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)});
    }

    private ContentValues ingredientValues(String name, double quantity, String unit, String expiryDate) {
        ContentValues values = new ContentValues();
        values.put("name", normalize(name));
        values.put("quantity", quantity);
        values.put("unit", unit.trim().toLowerCase(Locale.ROOT));
        values.put("expiry_date", expiryDate == null ? "" : expiryDate.trim());
        return values;
    }

    public Ingredient getIngredient(long id) {
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM pantry WHERE id=?", new String[]{String.valueOf(id)})) {
            return cursor.moveToFirst() ? ingredientFromCursor(cursor) : null;
        }
    }

    public List<Ingredient> getPantry() {
        List<Ingredient> pantry = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM pantry ORDER BY name COLLATE NOCASE", null)) {
            while (cursor.moveToNext()) pantry.add(ingredientFromCursor(cursor));
        }
        if (pantry.isEmpty()) {
            seedPantry(getWritableDatabase());
            try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM pantry ORDER BY name COLLATE NOCASE", null)) {
                while (cursor.moveToNext()) pantry.add(ingredientFromCursor(cursor));
            }
        }
        return pantry;
    }

    private Ingredient ingredientFromCursor(Cursor cursor) {
        return new Ingredient(cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                cursor.getString(cursor.getColumnIndexOrThrow("expiry_date")));
    }

    public List<Recipe> getRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM recipes ORDER BY name COLLATE NOCASE", null)) {
            while (cursor.moveToNext()) {
                long recipeId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                List<Recipe.RecipeIngredient> ingredients = new ArrayList<>();
                try (Cursor ingredientCursor = db.rawQuery("SELECT name, quantity, unit FROM recipe_ingredients WHERE recipe_id=?", new String[]{String.valueOf(recipeId)})) {
                    while (ingredientCursor.moveToNext()) {
                        ingredients.add(new Recipe.RecipeIngredient(ingredientCursor.getString(0), ingredientCursor.getDouble(1), ingredientCursor.getString(2)));
                    }
                }
                recipes.add(new Recipe(recipeId, cursor.getString(cursor.getColumnIndexOrThrow("name")), cursor.getString(cursor.getColumnIndexOrThrow("method")), ingredients));
            }
        }
        return recipes;
    }

    /** Returns only recipes for which every required ingredient and quantity is available. */
    public List<Recipe> getStrictMatches() {
        List<Recipe> matches = new ArrayList<>();
        List<Ingredient> pantry = getPantry();
        for (Recipe recipe : getRecipes()) {
            boolean canMake = true;
            for (Recipe.RecipeIngredient required : recipe.ingredients) {
                double available = 0;
                for (Ingredient item : pantry) {
                    if (normalize(item.name).equals(normalize(required.name)) && compatibleUnits(item.unit, required.unit)) {
                        available += convert(item.quantity, item.unit, required.unit);
                    }
                }
                if (available + 0.000001 < required.quantity) {
                    canMake = false;
                    break;
                }
            }
            if (canMake) matches.add(recipe);
        }
        return matches;
    }

    public static String normalize(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (normalized.endsWith("ies") && normalized.length() > 4) return normalized.substring(0, normalized.length() - 3) + "y";
        if (normalized.endsWith("oes") && normalized.length() > 4) return normalized.substring(0, normalized.length() - 2);
        if (normalized.endsWith("s") && !normalized.endsWith("ss") && normalized.length() > 3) return normalized.substring(0, normalized.length() - 1);
        return normalized;
    }

    private boolean compatibleUnits(String first, String second) {
        first = first.toLowerCase(Locale.ROOT);
        second = second.toLowerCase(Locale.ROOT);
        return first.equals(second) || (first.equals("kg") && second.equals("g")) || (first.equals("g") && second.equals("kg")) || (first.equals("l") && second.equals("ml")) || (first.equals("ml") && second.equals("l"));
    }

    private double convert(double amount, String from, String to) {
        from = from.toLowerCase(Locale.ROOT);
        to = to.toLowerCase(Locale.ROOT);
        if (from.equals(to)) return amount;
        if (from.equals("kg") && to.equals("g")) return amount * 1000;
        if (from.equals("g") && to.equals("kg")) return amount / 1000;
        if (from.equals("l") && to.equals("ml")) return amount * 1000;
        if (from.equals("ml") && to.equals("l")) return amount / 1000;
        return amount;
    }

    private void seedPantry(SQLiteDatabase db) {
        insertPantryItem(db, "tomato", 3, "piece", "2025-12-01");
        insertPantryItem(db, "egg", 6, "piece", "2025-11-30");
        insertPantryItem(db, "cheese", 200, "g", "2025-12-15");
        insertPantryItem(db, "milk", 1000, "ml", "2025-11-25");
        insertPantryItem(db, "bread", 8, "slice", "2025-11-18");
        insertPantryItem(db, "banana", 4, "piece", "2025-11-22");
        insertPantryItem(db, "oats", 500, "g", "2026-03-01");
        insertPantryItem(db, "garlic", 5, "clove", "2025-12-31");
        insertPantryItem(db, "pasta", 500, "g", "2026-06-01");
        insertPantryItem(db, "butter", 250, "g", "2025-12-20");
    }

    private void insertPantryItem(SQLiteDatabase db, String name, double quantity, String unit, String expiryDate) {
        ContentValues values = new ContentValues();
        values.put("name", normalize(name));
        values.put("quantity", quantity);
        values.put("unit", unit.trim().toLowerCase(Locale.ROOT));
        values.put("expiry_date", expiryDate == null ? "" : expiryDate.trim());
        db.insert("pantry", null, values);
    }

    private void seedRecipes(SQLiteDatabase db) {
        seed(db, "Tomato Egg Scramble", "1. Chop tomato. 2. Beat eggs. 3. Cook tomato, add egg and stir until set.", new String[]{"tomato", "egg"}, new double[]{1, 2}, new String[]{"piece", "piece"});
        seed(db, "Cheese Omelette", "1. Beat eggs. 2. Pour into a heated pan. 3. Add cheese, fold and cook.", new String[]{"egg", "cheese"}, new double[]{2, 30}, new String[]{"piece", "g"});
        seed(db, "Banana Oat Pancakes", "1. Mash banana. 2. Mix with oats and egg. 3. Pan-fry both sides.", new String[]{"banana", "oats", "egg"}, new double[]{1, 50, 1}, new String[]{"piece", "g", "piece"});
        seed(db, "Garlic Tomato Pasta", "1. Cook pasta. 2. Saute garlic and tomato. 3. Combine and serve.", new String[]{"pasta", "garlic", "tomato"}, new double[]{100, 1, 1}, new String[]{"g", "clove", "piece"});
        seed(db, "Potato Egg Hash", "1. Dice and cook potato. 2. Add egg. 3. Cook until set.", new String[]{"potato", "egg"}, new double[]{2, 1}, new String[]{"piece", "piece"});
        seed(db, "Apple Oat Bowl", "1. Chop apple. 2. Combine with oats and milk. 3. Serve.", new String[]{"apple", "oats", "milk"}, new double[]{1, 40, 150}, new String[]{"piece", "g", "ml"});
        seed(db, "Peanut Banana Toast", "1. Toast bread. 2. Spread peanut butter. 3. Add banana.", new String[]{"bread", "peanut butter", "banana"}, new double[]{2, 20, 1}, new String[]{"slice", "g", "piece"});
        seed(db, "Chicken Rice Bowl", "1. Cook rice. 2. Cook chicken thoroughly. 3. Combine and season.", new String[]{"chicken", "rice"}, new double[]{150, 80}, new String[]{"g", "g"});
        seed(db, "Tuna Mayo Sandwich", "1. Drain tuna. 2. Mix with mayonnaise. 3. Fill bread slices.", new String[]{"tuna", "mayonnaise", "bread"}, new double[]{1, 15, 2}, new String[]{"can", "g", "slice"});
        seed(db, "Carrot Potato Soup", "1. Chop carrot and potato. 2. Simmer until soft. 3. Blend or mash.", new String[]{"carrot", "potato"}, new double[]{2, 1}, new String[]{"piece", "piece"});
        seed(db, "Spinach Cheese Toast", "1. Wilt spinach. 2. Add spinach and cheese to bread. 3. Toast.", new String[]{"spinach", "cheese", "bread"}, new double[]{40, 30, 2}, new String[]{"g", "g", "slice"});
        seed(db, "Yoghurt Fruit Bowl", "1. Chop apple and banana. 2. Mix with yoghurt and serve.", new String[]{"apple", "banana", "yoghurt"}, new double[]{1, 1, 100}, new String[]{"piece", "piece", "g"});
        seed(db, "Garlic Butter Potatoes", "1. Boil potatoes. 2. Toss with butter and garlic.", new String[]{"potato", "butter", "garlic"}, new double[]{2, 15, 1}, new String[]{"piece", "g", "clove"});
        seed(db, "Simple Fried Rice", "1. Cook rice. 2. Scramble egg. 3. Stir-fry rice and peas.", new String[]{"rice", "egg", "peas"}, new double[]{150, 1, 40}, new String[]{"g", "piece", "g"});
        seed(db, "Tomato Cheese Toast", "1. Place tomato and cheese on bread. 2. Grill until melted.", new String[]{"tomato", "cheese", "bread"}, new double[]{1, 30, 2}, new String[]{"piece", "g", "slice"});
        seed(db, "Milk Banana Smoothie", "1. Add ingredients to blender. 2. Blend until smooth.", new String[]{"banana", "milk", "yoghurt"}, new double[]{1, 200, 50}, new String[]{"piece", "ml", "g"});
        seed(db, "Chicken Garlic Pasta", "1. Cook pasta. 2. Cook chicken with garlic. 3. Combine.", new String[]{"chicken", "garlic", "pasta"}, new double[]{100, 1, 100}, new String[]{"g", "clove", "g"});
        seed(db, "Carrot Apple Salad", "1. Grate carrot and apple. 2. Mix and serve.", new String[]{"carrot", "apple"}, new double[]{1, 1}, new String[]{"piece", "piece"});
        seed(db, "Pea Potato Mash", "1. Boil potato and peas. 2. Mash with butter.", new String[]{"potato", "peas", "butter"}, new double[]{2, 50, 10}, new String[]{"piece", "g", "g"});
        seed(db, "Cheesy Egg Sandwich", "1. Cook eggs. 2. Add cheese and place in bread.", new String[]{"egg", "cheese", "bread"}, new double[]{2, 20, 2}, new String[]{"piece", "g", "slice"});
        seed(db, "French Toast", "1. Whisk egg and milk. 2. Dip bread slices in mixture. 3. Fry in buttered pan until golden.", new String[]{"bread", "egg", "milk", "butter"}, new double[]{2, 1, 50, 10}, new String[]{"slice", "piece", "ml", "g"});
        seed(db, "Mushroom Swiss Omelette", "1. Saute mushrooms in butter. 2. Whisk eggs and pour into pan. 3. Add mushrooms and cheese, fold and cook.", new String[]{"egg", "mushroom", "cheese", "butter"}, new double[]{2, 50, 30, 10}, new String[]{"piece", "g", "g", "g"});
        seed(db, "Avocado Egg Toast", "1. Toast bread slices. 2. Mash avocado with seasoning. 3. Top toast with avocado and fried eggs.", new String[]{"bread", "avocado", "egg"}, new double[]{2, 1, 2}, new String[]{"slice", "piece", "piece"});
        seed(db, "Berry Oats Porridge", "1. Cook oats in milk until soft. 2. Stir in berries. 3. Serve warm.", new String[]{"oats", "milk", "berries"}, new double[]{60, 200, 50}, new String[]{"g", "ml", "g"});
        seed(db, "Garlic Butter Pasta", "1. Boil pasta. 2. Saute garlic in melted butter. 3. Toss pasta with garlic butter.", new String[]{"pasta", "garlic", "butter"}, new double[]{150, 2, 25}, new String[]{"g", "clove", "g"});
    }

    private void seed(SQLiteDatabase db, String name, String method, String[] ingredients, double[] quantities, String[] units) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", name);
        recipe.put("method", method);
        long recipeId = db.insert("recipes", null, recipe);
        for (int index = 0; index < ingredients.length; index++) {
            ContentValues ingredient = new ContentValues();
            ingredient.put("recipe_id", recipeId);
            ingredient.put("name", ingredients[index]);
            ingredient.put("quantity", quantities[index]);
            ingredient.put("unit", units[index]);
            db.insert("recipe_ingredients", null, ingredient);
        }
    }
}
