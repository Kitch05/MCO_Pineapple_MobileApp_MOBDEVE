package com.example.pineapple;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import android.content.SharedPreferences;

public class Login extends AppCompatActivity {

    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;

    Button btnGoogle;

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("PineapplePrefs", MODE_PRIVATE);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        ImageView togglePasswordVisibility = findViewById(R.id.toggle_password_visibility);
        TextView signupLink = findViewById(R.id.signup_link);
        TextView guestLink = findViewById(R.id.guest_link);
        Button loginButton = findViewById(R.id.login_button);
        CheckBox rememberMeCheckBox = findViewById(R.id.remember_me);

        // Check if user is already logged in
        checkRememberMe();

        btnGoogle = findViewById(R.id.btnGoogle);

        oneTapClient = Identity.getSignInClient(this);
        signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.web_client_id))
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        ActivityResultLauncher<IntentSenderRequest> activityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                try {
                                    // Get the SignInCredential
                                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                                    String idToken = credential.getGoogleIdToken();

                                    if (idToken != null) {
                                        // Authenticate with Firebase using the ID token
                                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                                        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        // Sign-in successful
                                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                        if (user != null) {
                                                            String email = user.getEmail();
                                                            String username = user.getDisplayName(); // Firebase provides the display name as "name"

                                                            // Save user details in Firestore
                                                            User newUser = new User(username, email); // Map displayName to username
                                                            db.collection("users").document(user.getUid())
                                                                    .set(newUser)
                                                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User added to Firestore"))
                                                                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding user", e));

                                                            // Navigate to the main activity
                                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    } else {
                                                        // Handle Firebase authentication errors
                                                        Toast.makeText(getApplicationContext(), "Authentication Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } catch (ApiException e) {
                                    Log.e("One Tap Sign-In", "Error getting credentials", e);
                                }
                            }
                        });



        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                oneTapClient.beginSignIn(signUpRequest)
                        .addOnSuccessListener(Login.this, new OnSuccessListener<BeginSignInResult>() {
                            @Override
                            public void onSuccess(BeginSignInResult result) {

                                IntentSenderRequest intentSenderRequest =
                                        new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                                activityResultLauncher.launch(intentSenderRequest);

                            }
                        })
                        .addOnFailureListener(Login.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // No Google Accounts found. Just continue presenting the signed-out UI.
                                Log.d("TAG", e.getLocalizedMessage());
                            }
                        });

            }
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

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Registration.class);
            startActivity(intent);
        });

        guestLink.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        loginButton.setOnClickListener(v -> {
            String input = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            } else {
                if (isValidEmail(input)) {
                    loginWithEmail(input, password, rememberMeCheckBox.isChecked());
                } else {
                    loginWithUsername(input, password, rememberMeCheckBox.isChecked());
                }
            }
        });
    }

    private void checkRememberMe() {
        String savedEmail = sharedPreferences.getString("email", null);
        if (savedEmail != null) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish(); // Skip the login screen
        }
    }

    private boolean isValidEmail(String input) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches();
    }

    private void loginWithEmail(String email, String password, boolean rememberMe) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(Login.this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (rememberMe) {
                            saveLoginDetails(email);
                        }
                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Email and Password do not match", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginWithUsername(final String username, final String password, boolean rememberMe) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (!documents.isEmpty()) {
                            String email = documents.getDocuments().get(0).getString("email");
                            if (email != null) {
                                if (rememberMe) {
                                    saveLoginDetails(email);
                                }
                                loginWithEmail(email, password, rememberMe);
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

    private void saveLoginDetails(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.apply(); // Save email in SharedPreferences
    }
}
