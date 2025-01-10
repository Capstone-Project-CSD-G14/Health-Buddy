package com.example.health_app_1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class NutritionResultActivity extends AppCompatActivity {

    private TextView tvFoodDetails, tvNutritionalNeeds, tvHealthSuggestions;
    private ToggleButton toggleWaterNotification, toggleHealthNotification;
    private ToggleButton toggleMorningMedicine, toggleAfternoonMedicine, toggleNightMedicine;
    private static final String CHANNEL_ID = "health_buddy_notifications";
    private String healthCondition; // Health condition will be set from the intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_result);

        // Initialize views
        tvFoodDetails = findViewById(R.id.tvFoodDetails);
        tvNutritionalNeeds = findViewById(R.id.tvNutritionalNeeds);
        tvHealthSuggestions = findViewById(R.id.tvHealthSuggestions);
        toggleWaterNotification = findViewById(R.id.toggleWaterNotification);
        toggleHealthNotification = findViewById(R.id.toggleHealthNotification);
        toggleMorningMedicine = findViewById(R.id.toggleMorningMedicine);
        toggleAfternoonMedicine = findViewById(R.id.toggleAfternoonMedicine);
        toggleNightMedicine = findViewById(R.id.toggleNightMedicine);

        // Setup notification channel
        createNotificationChannel();

        // Retrieve data from the intent
        Intent intent = getIntent();
        String selectedFood = intent.getStringExtra("food");
        int caloricValue = intent.getIntExtra("caloric Value", 0);
        double fat = intent.getDoubleExtra("Fat", 0);
        double protein = intent.getDoubleExtra("Protein", 0);
        healthCondition = intent.getStringExtra("healthCondition");
        String gender = intent.getStringExtra("gender"); // Get the gender from the intent

        if (selectedFood == null) {
            Toast.makeText(this, "No food data available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Display selected food details
        String foodDetails = String.format(
                "Selected Food: %s\nCaloric Value: %d kcal\nFat: %.2f g\nProtein: %.2f g",
                selectedFood, caloricValue, fat, protein
        );
        tvFoodDetails.setText(foodDetails);

        // Calculate recommended caloric and nutrient intake
        calculateNutritionalNeeds(caloricValue, fat, protein, gender); // Pass gender to the method

        // Provide health suggestions
        provideHealthSuggestions(healthCondition);

        // Toggle notification functionality
        setupToggleListeners();
    }

    private void setupToggleListeners() {
        toggleWaterNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            schedulePeriodicNotifications();
            Toast.makeText(this, isChecked ? "Water notifications enabled" : "Water notifications disabled", Toast.LENGTH_SHORT).show();
        });

        toggleHealthNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            schedulePeriodicNotifications();
            Toast.makeText(this, isChecked ? "Health notifications enabled" : "Health notifications disabled", Toast.LENGTH_SHORT).show();
        });

        toggleMorningMedicine.setOnCheckedChangeListener((buttonView, isChecked) -> {
            scheduleMedicineNotification("morning", isChecked);
            Toast.makeText(this, isChecked ? "Morning medicine notifications enabled" : "Morning medicine notifications disabled", Toast.LENGTH_SHORT).show();
        });

        toggleAfternoonMedicine.setOnCheckedChangeListener((buttonView, isChecked) -> {
            scheduleMedicineNotification("afternoon", isChecked);
            Toast.makeText(this, isChecked ? "Afternoon medicine notifications enabled" : "Afternoon medicine notifications disabled", Toast.LENGTH_SHORT).show();
        });

        toggleNightMedicine.setOnCheckedChangeListener((buttonView, isChecked) -> {
            scheduleMedicineNotification("night", isChecked);
            Toast.makeText(this, isChecked ? "Night medicine notifications enabled" : "Night medicine notifications disabled", Toast.LENGTH_SHORT).show();
        });
    }

    private void calculateNutritionalNeeds(int caloricValue, double fat, double protein, String gender) {
        // Retrieve user's physical details from intent
        Intent intent = getIntent();
        int age = intent.getIntExtra("age", 25); // Default age 25
        double weight = intent.getDoubleExtra("weight", 70); // Default weight 70 kg
        int height = intent.getIntExtra("height", 170); // Default height 170 cm
        double activityFactor = intent.getDoubleExtra("activityFactor", 1.2); // Default sedentary

        // Calculate BMR using Mifflin-St Jeor Equation
        double bmr;
        if ("Male".equalsIgnoreCase(gender)) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else if ("Female".equalsIgnoreCase(gender)) {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        } else {
            Toast.makeText(this, "Gender not specified. Defaulting to Male.", Toast.LENGTH_SHORT).show();
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        }

        // Adjust for activity level (sedentary, lightly active, etc.)
        double recommendedCalories = bmr * activityFactor;

        // Define percentage ranges for macronutrients (fat, protein, carbs)
        double fatPercentage = 0.30; // 30% of daily calories from fat
        double proteinPercentage = 0.20; // 20% of daily calories from protein
        double carbPercentage = 1 - (fatPercentage + proteinPercentage); // Remaining calories go to carbs

        // Calculate recommended intake for fat, protein, and carbs
        double recommendedFatCalories = recommendedCalories * fatPercentage;
        double recommendedProteinCalories = recommendedCalories * proteinPercentage;
        double recommendedCarbCalories = recommendedCalories * carbPercentage;

        // Convert calories into grams
        double recommendedFat = recommendedFatCalories / 9; // 9 calories per gram of fat
        double recommendedProtein = recommendedProteinCalories / 4; // 4 calories per gram of protein
        double recommendedCarbs = recommendedCarbCalories / 4; // 4 calories per gram of carbs

        // Display nutritional needs
        String nutritionalNeeds = String.format(
                "Recommended Daily Intake:\n- Calories: %.2f kcal\n- Fat: %.2f g\n- Protein: %.2f g\n- Carbs: %.2f g",
                recommendedCalories, recommendedFat, recommendedProtein, recommendedCarbs
        );
        tvNutritionalNeeds.setText(nutritionalNeeds);
    }


    private void provideHealthSuggestions(String healthCondition) {
        String suggestions;
        if (healthCondition == null || healthCondition.equals("None")) {
            suggestions = "Maintain a balanced diet and consult your doctor for personalized advice.";
        } else {
            switch (healthCondition) {
                case "Diabetes":
                    suggestions = "Maintain a low-carb diet, avoid sugary foods, and monitor your blood sugar regularly.";
                    break;
                case "Hypertension":
                    suggestions = "Limit salt intake, eat potassium-rich foods like bananas and spinach, and exercise regularly.";
                    break;
                case "Obesity":
                    suggestions = "Focus on a high-fiber, low-calorie diet, practice portion control, and incorporate regular physical activity.";
                    break;
                case "Anemia":
                    suggestions = "Include iron-rich foods such as leafy greens, lean meats, beans, and fortified cereals. Pair with vitamin C for better absorption.";
                    break;
                case "Low BP":
                    suggestions = "Increase fluid and salt intake, eat small and frequent meals, and avoid sudden changes in posture.";
                    break;
                case "High BP":
                    suggestions = "Follow a DASH diet, reduce sodium intake, and include whole grains, fruits, and vegetables in your meals.";
                    break;
                case "Thyroid":
                    suggestions = "For hypothyroidism, consume iodine-rich foods like seaweed and avoid goitrogens. For hyperthyroidism, manage iodine intake under medical advice.";
                    break;
                case "High Cholesterol":
                    suggestions = "Eat heart-healthy foods like oats, nuts, fatty fish, and olive oil. Limit saturated and trans fats.";
                    break;
                default:
                    suggestions = "Maintain a balanced diet and stay healthy.";
                    break;
            }
        }

        tvHealthSuggestions.setText("Health Suggestions:\n" + suggestions);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "HealthBuddy Notifications";
            String description = "Notifications for health and water reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void schedulePeriodicNotifications() {
        // Schedule notifications based on current toggle states
        if (toggleWaterNotification.isChecked()) {
            scheduleNotification("hydration", null);
        }
        if (toggleHealthNotification.isChecked()) {
            scheduleNotification("health", healthCondition);
        }
    }

    private void scheduleMedicineNotification(String timeOfDay, boolean isEnabled) {
        if (isEnabled) {
            long delay = getDelayForMedicineNotification(timeOfDay);
            scheduleNotification(timeOfDay + "_medicine", null, delay);
        }
    }

    private long getDelayForMedicineNotification(String timeOfDay) {
        // Calculate delay based on the time of day
        long currentTime = System.currentTimeMillis();
        long notificationTime = 0;

        switch (timeOfDay) {
            case "morning":
                notificationTime = getNextTime(currentTime, 8, 0); // 8 AM
                break;
            case "afternoon":
                notificationTime = getNextTime(currentTime, 14, 0); // 2 PM
                break;
            case "night":
                notificationTime = getNextTime(currentTime, 20, 0); // 8 PM
                break;
        }

        return notificationTime - currentTime;
    }

    private long getNextTime(long currentTime, int hour, int minute) {
        // Calculate the next occurrence of the specified time
        long targetTime = System.currentTimeMillis();
        // Placeholder for next time calculation logic (you can implement the exact time calculation here)
        return targetTime + TimeUnit.SECONDS.toMillis(60); // Just for testing
    }

    private void scheduleNotification(String type, String healthCondition) {
        scheduleNotification(type, healthCondition, 0);
    }

    private void scheduleNotification(String type, String healthCondition, long delay) {
        Data inputData = new Data.Builder()
                .putString("notificationType", type)
                .putString("healthCondition", healthCondition)
                .build();

        PeriodicWorkRequest notificationWorkRequest = new PeriodicWorkRequest.Builder(
                NotificationWorker.class, 15, TimeUnit.MINUTES) // Set to 15 mins for demo
                .setInputData(inputData)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueue(notificationWorkRequest);
    }
}