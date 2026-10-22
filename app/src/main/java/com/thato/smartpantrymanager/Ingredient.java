package com.thato.smartpantrymanager;
public class Ingredient {

    public int id;
    public String name;
    public double quantity;
    public String unit;


public Ingredient(int id, String name, double quantity, String unit){
    this.id = id;
    this.name = name;
    this.quantity = quantity;
    this.unit = unit;
}

// converts the unit from 1.0 to just 1
public String quantityText() {
    if (quantity == (long) quantity) {
        return String.valueOf((long) quantity);
    }
    return String.valueOf(quantity);
}

public String amountText(){
    return quantityText() + " " + unit;
}
}
