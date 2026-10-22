package za.co.richfield.smartpantry;

public class Ingredient {
    public final long id;
    public final String name;
    public final double quantity;
    public final String unit;
    public final String expiryDate;

    public Ingredient(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate == null ? "" : expiryDate;
    }
}

