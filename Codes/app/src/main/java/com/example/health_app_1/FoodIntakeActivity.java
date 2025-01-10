package com.example.health_app_1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class FoodIntakeActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteFood;
    private Spinner spinnerHealthConditions;
    private Button btnPrevious, btnNext, btnAddFood;
    private LinearLayout foodContainer;

    private HashMap<String, JSONObject> foodDetailsMap = new HashMap<>();
    private ArrayList<String> foodList = new ArrayList<>();
    private ArrayList<AutoCompleteTextView> autoCompleteTextViews = new ArrayList<>();

    private String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_intake);

        // Initialize UI elements
        autoCompleteFood = findViewById(R.id.auto_complete_food);
        spinnerHealthConditions = findViewById(R.id.spinner_health_conditions);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnAddFood = findViewById(R.id.btn_add_food);
        foodContainer = findViewById(R.id.food_container);

        // Retrieve gender from Intent
        Intent intent = getIntent();
        gender = intent.getStringExtra("gender");
        Log.d("MainActivity2", "Received Gender: " + gender);

        // Load food data from JSON
        loadFoodDataFromJson();

        // Setup AutoComplete for food
        ArrayAdapter<String> foodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, foodList);
        autoCompleteFood.setAdapter(foodAdapter);

        // Setup health conditions spinner
        ArrayAdapter<String> healthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"None", "Diabetes", "Hypertension", "Obesity", "Anemia", "Low BP", "High BP", "Thyroid",
                        "High Cholesterol", "Supraventricular Tachycardia", "Anal Fissure", "Barrett's Esophagus (BE)",
                        "Gallstones", "Piles (Hemorrhoids)", "Endometriosis", "Fibroids", "Hiatal Hernia"});
        healthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHealthConditions.setAdapter(healthAdapter);

        // Button Listeners
        btnPrevious.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> navigateToNextPage());

        btnAddFood.setOnClickListener(v -> addFoodAutoComplete());
    }

    private void loadFoodDataFromJson() {
        try {
            InputStream is = getAssets().open("food_d.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONObject root = new JSONObject(json);
            JSONArray csvFiles = root.getJSONArray("csv files");

            for (int i = 0; i < csvFiles.length(); i++) {
                JSONObject foodItem = csvFiles.getJSONObject(i);
                String foodName = foodItem.getString("food");
                foodList.add(foodName);
                foodDetailsMap.put(foodName, foodItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading food data", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFoodAutoComplete() {
        AutoCompleteTextView newAutoComplete = new AutoCompleteTextView(this);
        newAutoComplete.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        newAutoComplete.setHint("Type food name");
        newAutoComplete.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, foodList));
        foodContainer.addView(newAutoComplete);
        autoCompleteTextViews.add(newAutoComplete);
    }

    private void navigateToNextPage() {
        String healthCondition = spinnerHealthConditions.getSelectedItem().toString();
        double totalCalories = 0;
        double totalFat = 0;
        double totalProtein = 0;
        int validFoodCount = 0;

        // Iterate through all the AutoCompleteTextView inputs
        for (AutoCompleteTextView autoComplete : autoCompleteTextViews) {
            String selectedFood = autoComplete.getText().toString().trim();
            if (!selectedFood.isEmpty() && foodDetailsMap.containsKey(selectedFood)) {
                try {
                    // Retrieve food details from the HashMap
                    JSONObject foodDetails = foodDetailsMap.get(selectedFood);

                    // Safely fetch the nutritional values using optDouble (default to 0 if missing)
                    double calories = foodDetails.optDouble("caloric Value", 0);
                    double fat = foodDetails.optDouble("Fat", 0);
                    double protein = foodDetails.optDouble("Protein", 0);

                    // Add values to the totals
                    totalCalories += calories;
                    totalFat += fat;
                    totalProtein += protein;
                    validFoodCount++;

                    Log.d("FoodSelection", "Valid food selected: " + selectedFood +
                            " | Calories: " + calories + ", Fat: " + fat + ", Protein: " + protein);
                } catch (Exception e) {
                    Log.e("FoodSelection", "Error processing food: " + selectedFood, e);
                    Toast.makeText(this, "Error retrieving details for: " + selectedFood, Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("FoodSelection", "Invalid or missing food selected: " + selectedFood);
            }
        }

        // Check if at least one valid food item was selected
        if (validFoodCount == 0) {
            Toast.makeText(this, "Please select at least one valid food item", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate averages
        int averageCalories = (int) (totalCalories / validFoodCount); // Round to nearest integer
        double averageFat = totalFat / validFoodCount;
        double averageProtein = totalProtein / validFoodCount;

        // Log the calculated values
        Log.d("FoodSelection", "Average Calories: " + averageCalories +
                ", Average Fat: " + averageFat + ", Average Protein: " + averageProtein);

        // Pass data to MainActivity3
        Intent intent = new Intent(FoodIntakeActivity.this, NutritionResultActivity.class);
        intent.putExtra("food", "Multiple Foods"); // Indicate multiple food items
        intent.putExtra("caloric Value", averageCalories);
        intent.putExtra("Fat", averageFat);
        intent.putExtra("Protein", averageProtein);
        intent.putExtra("healthCondition", healthCondition);
        intent.putExtra("gender", gender);  // Pass gender to the next activity
        startActivity(intent);
    }
}