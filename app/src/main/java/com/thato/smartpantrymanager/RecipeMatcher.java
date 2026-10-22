package com.thato.smartpantrymanager;
import java.util.ArrayList;
import java.util.List;

// the recipemather decides which ingredients a recipe is missing
// 0 - this is a valid match and the recipe is suggested
// 1 to 3 missing ingredients, they will be kept in the almost there list
// 4 or more ingredients will not show any ingredients

public class RecipeMatcher {

    // lower-case, trim so that Tomatoes matches tomato
    public static String normalise(String name) {
        String n = name.trim().toLowerCase().replaceAll("\\s+", " ");
        if (n.endsWith("ies") && n.length() > 4) {
            return n.substring(0, n.length() - 3) + "y";   // cherries -> cherry
        }
        if (n.endsWith("oes")) {
            return n.substring(0, n.length() - 2);          // tomatoes -> tomato
        }
        if (n.endsWith("s") && !n.endsWith("ss")) {
            return n.substring(0, n.length() - 1);          // eggs -> egg
        }
        return n;
    }

    // changes kg to g and l to ml so that different quantities can be compared
    public static double toBase(double quantity, String unit) {
        if (unit.equals("kg") || unit.equals("l")) {
            return quantity * 1000;
        }
        return quantity;
    }

    // weight (g/kg) volume (ml/l) or count (pcs)
    public static String unitType(String unit) {
        if (unit.equals("g") || unit.equals("kg")) {
            return "weight";
        }
        if (unit.equals("ml") || unit.equals("l")) {
            return "volume";
        }
        return "count";
    }

    // returns the names of the recipe ingredients the user does NOT have enough of to make the meal
    public static List<String> findMissing(Recipe recipe, List<Ingredient> pantry) {
        List<String> missing = new ArrayList<>();

        for (Ingredient needed : recipe.ingredients) {
            String neededName = normalise(needed.name);
            double required = toBase(needed.quantity, needed.unit);

            double have = 0;
            boolean nameFound = false;
            boolean differentUnit = false;

            for (Ingredient item : pantry) {
                if (!normalise(item.name).equals(neededName)) {
                    continue;
                }
                nameFound = true;
                if (unitType(item.unit).equals(unitType(needed.unit))) {
                    // adds up the duplicates
                    have += toBase(item.quantity, item.unit);
                } else {
                    differentUnit = true;
                }
            }

            if (!nameFound) {
                missing.add(needed.name);
            } else if (have + 0.0001 >= required) {
                // enough - nothing to do
            } else if (differentUnit) {
                // units cannot be compared
            } else {
                // there aren't enough quantities of the ingredient
                missing.add(needed.name);
            }
        }
        return missing;
    }
}
