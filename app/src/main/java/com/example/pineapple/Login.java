package com.example.pineapple;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Login extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        ImageView togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);
        TextView signupLink = findViewById(R.id.signup_link);
        TextView guestLink = findViewById(R.id.guest_link);
        Button loginButton = findViewById(R.id.login_button);

        signupLink.setPaintFlags(signupLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        togglePasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    togglePasswordVisibility.setImageResource(R.drawable.hide_pw);
                } else {
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    togglePasswordVisibility.setImageResource(R.drawable.show_pw);
                }
                isPasswordVisible = !isPasswordVisible;
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
            }
        });

        guestLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (input.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                } else {
                    if (isValidEmail(input)) {
                        // If the input is a valid email, use Firebase Authentication
                        loginWithEmail(input, password);
                    } else {
                        // Otherwise, search Firestore for the username to find the associated email
                        loginWithUsername(input, password);
                    }
                }
            }
        });
    }

    // Check if the input is a valid email
    private boolean isValidEmail(String input) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches();
    }

    // Login using email
    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(Login.this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Email and Password do not match", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Login using username (search for the email in Firestore)
    private void loginWithUsername(final String username, final String password) {
        db.collection("users")
                .whereEqualTo("username", username) // Query the username
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (!documents.isEmpty()) {
                            String email = documents.getDocuments().get(0).getString("email");
                            if (email != null) {
                                loginWithEmail(email, password);  // Login with the retrieved email
                            } else {
                                Toast.makeText(Login.this, "Username not associated with any email", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Login.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Login.this, "Error retrieving username", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}