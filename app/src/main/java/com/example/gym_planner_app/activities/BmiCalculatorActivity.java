package com.example.gym_planner_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;

import java.util.Locale;

public class BmiCalculatorActivity extends AppCompatActivity {

    EditText etHeight, etWeight;
    Button btnCalculateBmi, btnBackHome;
    TextView tvBmiResult, tvBmiCategory;

    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calculator);

        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        btnCalculateBmi = findViewById(R.id.btnCalculateBmi);
        btnBackHome = findViewById(R.id.btnBackHome);
        tvBmiResult = findViewById(R.id.tvBmiResult);
        tvBmiCategory = findViewById(R.id.tvBmiCategory);

        userId = getIntent().getIntExtra("userId", -1);

        btnCalculateBmi.setOnClickListener(v -> calculateBmi());

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(BmiCalculatorActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }

    private void calculateBmi() {
        String heightText = etHeight.getText().toString().trim();
        String weightText = etWeight.getText().toString().trim();

        if (heightText.isEmpty() || weightText.isEmpty()) {
            Toast.makeText(this, "Please enter height and weight", Toast.LENGTH_SHORT).show();
            return;
        }

        double heightCm;
        double weightKg;

        try {
            heightCm = Double.parseDouble(heightText);
            weightKg = Double.parseDouble(weightText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (heightCm <= 0 || weightKg <= 0) {
            Toast.makeText(this, "Height and weight must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        double heightMeters = heightCm / 100.0;
        double bmi = weightKg / (heightMeters * heightMeters);

        String category;

        if (bmi < 18.5) {
            category = "Underweight";
        } else if (bmi < 25) {
            category = "Normal weight";
        } else if (bmi < 30) {
            category = "Overweight";
        } else {
            category = "Obese";
        }

        tvBmiResult.setText(String.format(Locale.getDefault(), "Your BMI: %.1f", bmi));
        tvBmiCategory.setText("Category: " + category);
    }
}