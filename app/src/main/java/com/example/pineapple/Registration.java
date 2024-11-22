package com.example.pineapple;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Registration extends AppCompatActivity {

    private EditText passwordEditText;
    private EditText emailEditText;
    private EditText confirmPasswordEditText;
    private EditText usernameEditText;  // Add a username field
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email);
        usernameEditText = findViewById(R.id.username);  // Add a username input field
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        Button signupButton = findViewById(R.id.signup_button);
        TextView loginLink = findViewById(R.id.login_link);
        ImageView togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);
        ImageView toggleConfirmPasswordVisibility = findViewById(R.id.toggle_confirm_password_visibility);

        loginLink.setPaintFlags(loginLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        signupButton.setOnClickListener(v -> {
            if (validateForm()) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();  // Get the username

                // Firebase registration logic
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(Registration.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                                // Save the username and email to Firestore
                                saveUserToFirestore(user, username, email);

                                // Navigate to login page
                                Intent intent = new Intent(Registration.this, Login.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Registration.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
        });

        togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePasswordVisibility.setImageResource(R.drawable.hide_pw);
            } else {
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                togglePasswordVisibility.setImageResource(R.drawable.show_pw);
            }
            isPasswordVisible = !isPasswordVisible;
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        toggleConfirmPasswordVisibility.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggleConfirmPasswordVisibility.setImageResource(R.drawable.hide_pw);
            } else {
                confirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                toggleConfirmPasswordVisibility.setImageResource(R.drawable.show_pw);
            }
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
        });
    }

    private void saveUserToFirestore(FirebaseUser user, String username, String email) {
        // Create a map with the username and email
        db.collection("users").document(user.getUid())  // Use the Firebase UID as document ID
                .set(new User(username, email))  // Save the username and email in Firestore
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved user data to Firestore
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Registration.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateForm() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Not a Valid Email Address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password and confirm password do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Create a User class to save to Firestore
    public class User {
        public String username;
        public String email;

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }
    }
}
