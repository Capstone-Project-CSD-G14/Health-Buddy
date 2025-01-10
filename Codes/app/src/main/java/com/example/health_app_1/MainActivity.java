package com.example.health_app_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etAge, etHeight, etWeight;
    private RadioGroup rgGender;
    private Spinner spinnerBloodGroup;
    private Button btnCalculateBMI, btnNextPage;
    private TextView tvBMIResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        rgGender = findViewById(R.id.rgGender);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);
        btnCalculateBMI = findViewById(R.id.btnCalculateBMI);
        btnNextPage = findViewById(R.id.btnNextPage);
        tvBMIResult = findViewById(R.id.tvBMIResult);

        // Setup Blood Group Spinner
        setupBloodGroupSpinner();

        btnCalculateBMI.setOnClickListener(v -> calculateBMI());

        btnNextPage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FoodIntakeActivity.class);
            startActivity(intent);
        });
    }

    private void setupBloodGroupSpinner() {
        // Define the list of blood groups
        String[] bloodGroups = {"Select Blood Group", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                bloodGroups
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(adapter);

        // Handle item selection
        spinnerBloodGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBloodGroup = parent.getItemAtPosition(position).toString();
                if (!selectedBloodGroup.equals("Select Blood Group")) {
                    Toast.makeText(MainActivity.this, "Selected: " + selectedBloodGroup, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void calculateBMI() {
        // Validate inputs
        if (etHeight.getText().toString().isEmpty() || etWeight.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double height = Double.parseDouble(etHeight.getText().toString()) / 100; // Convert cm to meters
        double weight = Double.parseDouble(etWeight.getText().toString());

        // Calculate BMI
        double bmi = weight / (height * height);

        // Determine BMI Category
        String bmiCategory;
        if (bmi < 18.5) {
            bmiCategory = "Underweight";
        } else if (bmi < 24.9) {
            bmiCategory = "Normal";
        } else if (bmi < 29.9) {
            bmiCategory = "Overweight";
        } else {
            bmiCategory = "Obese";
        }

        // Display BMI and Category
        tvBMIResult.setText(String.format("Your BMI: %.2f\nCategory: %s", bmi, bmiCategory));
    }

    public void readFileFromAssets(Context context) {
        AssetManager assetManager = context.getAssets();

        try {
            InputStream is = assetManager.open("food_data.json"); // Replace with your file name
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Process the data
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}