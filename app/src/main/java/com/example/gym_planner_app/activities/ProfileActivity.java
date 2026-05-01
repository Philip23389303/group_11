package com.example.gym_planner_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;
import com.example.gym_planner_app.database.DatabaseHelper;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    Button btnBackHome, btnUpdatePassword;
    EditText etCurrentPassword, etNewPassword, etConfirmPassword;

    int userId;
    DatabaseHelper databaseHelper;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseHelper = new DatabaseHelper(this);
        mAuth = FirebaseAuth.getInstance();

        userId = getIntent().getIntExtra("userId", -1);

        btnBackHome = findViewById(R.id.btnBackHome);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnUpdatePassword.setOnClickListener(v -> updatePassword());

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }

    private void updatePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            Toast.makeText(this, "Firebase user not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = firebaseUser.getEmail();

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        firebaseUser.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (!reauthTask.isSuccessful()) {
                Toast.makeText(
                        ProfileActivity.this,
                        "Current password is incorrect",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            firebaseUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                if (updateTask.isSuccessful()) {

                    boolean localUpdated = databaseHelper.updateUserPasswordByEmail(email, newPassword);

                    if (localUpdated) {
                        Toast.makeText(
                                ProfileActivity.this,
                                "Password updated successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        etCurrentPassword.setText("");
                        etNewPassword.setText("");
                        etConfirmPassword.setText("");

                    } else {
                        Toast.makeText(
                                ProfileActivity.this,
                                "Firebase password updated, but local user record was not found",
                                Toast.LENGTH_LONG
                        ).show();

                        etCurrentPassword.setText("");
                        etNewPassword.setText("");
                        etConfirmPassword.setText("");
                    }

                } else {
                    String errorMessage = "Failed to update Firebase password";

                    if (updateTask.getException() != null &&
                            updateTask.getException().getMessage() != null) {
                        errorMessage = updateTask.getException().getMessage();
                    }

                    Toast.makeText(
                            ProfileActivity.this,
                            errorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        });
    }
}