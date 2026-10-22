package com.thato.smartpantrymanager;
import java.util.ArrayList;
import java.util.List;

// meals that are stored in the database, with the ingredients it need.
public class Recipe {
    public int id;
    public String name;
    public String steps;
    public List<Ingredient> ingredients = new ArrayList<>();

    // the message will be shown after its filled in by the recipe matcher algorithm
    public int missingCount = 0;
    public String infoText = "";

    public Recipe(){}

    public Recipe(int id, String name, String steps){
        this.id = id;
        this.name = name;
        this.steps = steps;
    }
}
