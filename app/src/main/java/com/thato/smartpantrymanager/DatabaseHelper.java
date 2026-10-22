package com.thato.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smartpantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    // the oncreate function will run once and create the database
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE users (" + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "username TEXT UNIQUE NOT NULL, " + "password TEXT NOT NULL)");

        db.execSQL("CREATE TABLE ingredients (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "user_id INTEGER NOT NULL, " + "name TEXT NOT NULL, " + "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " + "FOREIGN KEY (user_id) REFERENCES users(id))");

        db.execSQL("CREATE TABLE recipes (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " + "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE recipe_ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "FOREIGN KEY (recipe_id) REFERENCES recipes(id))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS ingredients");
        db.execSQL("DROP TABLE IF EXISTS users");
    }

    /*
    users
    */
    // this will return true if the user account exists and d=false if the username is taken
    public boolean registerUser(String username, String password){
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", hash(password));
        long result = getWritableDatabase().insert("users", null, values);
        return result != -1;
    }

    // this will return the users id if the details are correct
    public int loginUser(String username, String password){
        Cursor c = getReadableDatabase().rawQuery("SELECT id FROM users WHERE username = ? AND password = ?",
                new String[]{username, hash(password)});
        int userId = -1;
        if (c.moveToFirst()){
            userId = c.getInt(0);
        }
        c.close();
        return userId;
    }

    // method to hash passwords instead of being stored as plain text
    private String hash(String text){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e){
            return text;
        }
    }

    // pantry ingredients, users will be able to update, create and delete their items
    public void addIngredient(int userId, String name, double quantity, String unit){
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);
        getWritableDatabase().insert("ingredients", null, values);
    }

    // read
    public List<Ingredient> getIngredient(int userId){
        List<Ingredient> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, quantity, unit FROM ingredients " + "WHERE user_id = ? ORDER BY NAME COLLATE NOCASE",
                new String[]{String.valueOf(userId)});
        while (c.moveToNext()){
            list.add(new Ingredient(c.getInt(0), c.getString(1), c.getDouble(2), c.getString(3)));
        }
        c.close();
        return list;
    }

    // update
    public void updateIngredient(int id, String name, double quantity, String unit){
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);
        getWritableDatabase().update("ingredients", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // delete
    public void deleteIngredient(int id) {
        getWritableDatabase().delete("ingredients", "id = ?",
                new String[]{String.valueOf(id)});
    }

    // recipes: these recipes are read-only
    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, steps FROM recipes ORDER BY name", null);
        while (c.moveToNext()) {
            Recipe recipe = new Recipe();
            recipe.id = c.getInt(0);
            recipe.name = c.getString(1);
            recipe.steps = c.getString(2);
            recipe.ingredients = getRecipeIngredients(db, recipe.id);
            list.add(recipe);
        }
        c.close();
        return list;
    }

    public Recipe getRecipe(int recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, steps FROM recipes WHERE id = ?",
                new String[]{String.valueOf(recipeId)});
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(c.getInt(0), c.getString(1), c.getString(2));
            recipe.ingredients = getRecipeIngredients(db, recipe.id);
        }
        c.close();
        return recipe;
    }

    private List<Ingredient> getRecipeIngredients(SQLiteDatabase db, int recipeId) {
        List<Ingredient> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT id, name, quantity, unit FROM recipe_ingredients WHERE recipe_id = ?",
                new String[]{String.valueOf(recipeId)});
        while (c.moveToNext()) {
            list.add(new Ingredient(c.getInt(0), c.getString(1), c.getDouble(2), c.getString(3)));
        }
        c.close();
        return list;
    }

    //  SEED DATA - 20 recipes loaded the first time the app runs
    //  Ingredient format:  "name|quantity|unit"

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Whisk the eggs in a bowl.\n2. Melt the butter in a pan.\n3. Pour in the eggs and stir gently until just set.\n4. Toast the bread and serve the eggs on top.",
                "egg|3|pcs", "bread|2|pcs", "butter|10|g");

        addRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs.\n2. Melt the butter in a pan and pour in the eggs.\n3. Sprinkle the cheese over one half.\n4. Fold the omelette over and cook for 1 minute.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g");

        addRecipe(db, "Vegetable Omelette",
                "1. Chop the onion and tomato.\n2. Fry the onion until soft, then add the tomato.\n3. Pour in the whisked eggs and top with cheese.\n4. Fold and serve when set.",
                "egg|3|pcs", "onion|1|pcs", "tomato|1|pcs", "cheese|30|g");

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Place the cheese between the slices.\n3. Fry on both sides until golden and the cheese melts.",
                "bread|2|pcs", "cheese|60|g", "butter|10|g");

        addRecipe(db, "French Toast",
                "1. Whisk the eggs and milk together.\n2. Dip each slice of bread into the mixture.\n3. Fry in butter until golden on both sides.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "butter|10|g");

        addRecipe(db, "Pancakes",
                "1. Mix the flour, eggs and milk into a smooth batter.\n2. Melt a little butter in a pan.\n3. Pour in a ladle of batter and cook until bubbles form.\n4. Flip and cook the other side.",
                "flour|200|g", "egg|2|pcs", "milk|300|ml", "butter|20|g");

        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas in a bowl.\n2. Mix in the eggs and flour.\n3. Spoon small rounds into a hot pan.\n4. Cook until golden on both sides.",
                "banana|2|pcs", "egg|2|pcs", "flour|100|g");

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until soft and drain.\n2. Fry the chopped onion and garlic in olive oil.\n3. Add the chopped tomatoes and simmer for 10 minutes.\n4. Mix the sauce through the pasta.",
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "olive oil|20|ml");

        addRecipe(db, "Beef Mince and Pasta",
                "1. Boil the pasta and drain.\n2. Fry the onion, then brown the mince.\n3. Add the chopped tomatoes and simmer for 15 minutes.\n4. Serve the mince sauce over the pasta.",
                "mince|250|g", "pasta|200|g", "tomato|3|pcs", "onion|1|pcs");

        addRecipe(db, "Tuna Pasta",
                "1. Boil the pasta and drain.\n2. Fry the chopped onion in olive oil.\n3. Add the tuna and heat through.\n4. Toss with the pasta.",
                "pasta|200|g", "tuna|1|pcs", "onion|1|pcs", "olive oil|15|ml");

        addRecipe(db, "Pap and Tomato Gravy",
                "1. Boil water and slowly stir in the maize meal to make stiff pap.\n2. Fry the onion in oil, then add the chopped tomatoes.\n3. Simmer the gravy for 10 minutes.\n4. Serve the gravy over the pap.",
                "maize meal|250|g", "tomato|3|pcs", "onion|1|pcs", "oil|20|ml");

        addRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry the onion and grated carrot in oil.\n3. Push to the side, scramble the eggs in the pan.\n4. Add the rice and stir-fry for 3 minutes.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "carrot|1|pcs", "oil|15|ml");

        addRecipe(db, "Chicken and Rice",
                "1. Cut the chicken into pieces and fry with the onion in oil.\n2. Add the rice and stir for 1 minute.\n3. Cover with water and simmer until the rice is cooked.",
                "chicken|300|g", "rice|200|g", "onion|1|pcs", "oil|15|ml");

        addRecipe(db, "Chicken Stir-Fry",
                "1. Slice the chicken, carrots and onion.\n2. Fry the chicken in oil until cooked.\n3. Add the vegetables and cook for 5 minutes.\n4. Stir in the soy sauce and serve.",
                "chicken|300|g", "carrot|2|pcs", "onion|1|pcs", "oil|15|ml", "soy sauce|20|ml");

        addRecipe(db, "Chicken Mayo Sandwich",
                "1. Cook and shred the chicken.\n2. Mix the chicken with the mayonnaise.\n3. Spread on the bread and close the sandwiches.",
                "chicken|150|g", "mayonnaise|30|g", "bread|4|pcs");

        addRecipe(db, "Mashed Potato",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash with the butter and milk.",
                "potato|4|pcs", "butter|30|g", "milk|100|ml");

        addRecipe(db, "Cheesy Potato Bake",
                "1. Slice the potatoes and boil for 5 minutes.\n2. Layer in a dish with butter and cheese.\n3. Pour over the milk.\n4. Bake at 180 C for 30 minutes.",
                "potato|4|pcs", "cheese|80|g", "milk|100|ml", "butter|20|g");

        addRecipe(db, "Fried Potatoes",
                "1. Cut the potatoes into cubes.\n2. Fry in oil with the sliced onion until golden and crispy.",
                "potato|4|pcs", "oil|30|ml", "onion|1|pcs");

        addRecipe(db, "Vegetable Soup",
                "1. Chop the potatoes, carrots, onion and tomatoes.\n2. Fry the onion, then add the other vegetables.\n3. Cover with water and simmer for 25 minutes.",
                "potato|2|pcs", "carrot|2|pcs", "onion|1|pcs", "tomato|2|pcs");

        addRecipe(db, "Rice and Beans",
                "1. Cook the rice.\n2. Fry the onion and tomatoes.\n3. Add the beans and simmer for 10 minutes.\n4. Serve over the rice.",
                "rice|200|g", "beans|400|g", "onion|1|pcs", "tomato|2|pcs");
    }

    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("steps", steps);
        long recipeId = db.insert("recipes", null, recipeValues);

        for (String line : ingredients) {
            String[] parts = line.split("\\|");
            ContentValues values = new ContentValues();
            values.put("recipe_id", recipeId);
            values.put("name", parts[0]);
            values.put("quantity", Double.parseDouble(parts[1]));
            values.put("unit", parts[2]);
            db.insert("recipe_ingredients", null, values);
        }
    }

}
