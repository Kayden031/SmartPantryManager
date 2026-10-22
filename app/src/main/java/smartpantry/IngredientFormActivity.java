package za.co.richfield.smartpantry;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class IngredientFormActivity extends Activity {
    private DatabaseHelper database;
    private long ingredientId;
    private EditText nameField, quantityField, unitField, expiryField;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new DatabaseHelper(this);
        ingredientId = getIntent().getLongExtra("ingredient_id", -1);
        LinearLayout root = Ui.page(this, ingredientId == -1 ? "Add Ingredient" : "Edit Ingredient");
        nameField = field(root, "Ingredient name (for example: tomato)");
        quantityField = field(root, "Quantity (number)");
        unitField = field(root, "Unit (for example: piece, g, ml)");
        expiryField = field(root, "Expiry date - optional (YYYY-MM-DD)");

        if (ingredientId == -1) {
            quantityField.setText("1");
            unitField.setText("piece");
        } else {
            Ingredient ingredient = database.getIngredient(ingredientId);
            if (ingredient != null) {
                nameField.setText(ingredient.name);
                quantityField.setText(String.valueOf(ingredient.quantity));
                unitField.setText(ingredient.unit);
                expiryField.setText(ingredient.expiryDate);
            }
        }
        root.addView(Ui.button(this, "Save ingredient"));
        root.getChildAt(root.getChildCount() - 1).setOnClickListener(view -> save());
        root.addView(Ui.button(this, "Cancel"));
        root.getChildAt(root.getChildCount() - 1).setOnClickListener(view -> finish());
        setContentView(Ui.scroll(this, root));
    }

    private EditText field(LinearLayout root, String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setSingleLine(true);
        root.addView(field, new LinearLayout.LayoutParams(-1, -2));
        return field;
    }

    private void save() {
        String name = nameField.getText().toString().trim();
        String quantityText = quantityField.getText().toString().trim();
        String unit = unitField.getText().toString().trim();
        if (name.isEmpty() || quantityText.isEmpty() || unit.isEmpty()) {
            Toast.makeText(this, "Name, quantity and unit are required.", Toast.LENGTH_SHORT).show();
            if (name.isEmpty()) nameField.setError("Required");
            if (quantityText.isEmpty()) quantityField.setError("Required");
            if (unit.isEmpty()) unitField.setError("Required");
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            quantityField.setError("Enter a number greater than zero");
            return;
        }
        if (ingredientId == -1) database.addIngredient(name, quantity, unit, expiryField.getText().toString());
        else database.updateIngredient(ingredientId, name, quantity, unit, expiryField.getText().toString());
        Toast.makeText(this, "Ingredient saved.", Toast.LENGTH_SHORT).show();
        finish();
    }
}

