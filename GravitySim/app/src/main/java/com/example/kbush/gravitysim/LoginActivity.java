package com.example.kbush.gravitysim;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String returnName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_signup);

        initializeLoginButton();
        initializeSignupButton();

        mAuth = FirebaseAuth.getInstance();

        Bundle b = getIntent().getExtras();
        if (b != null && b.getBoolean("logout")) {
            mAuth.signOut();
        }
        // Login user
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            returnName = currentUser.getDisplayName();
            exitActivity();
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }

    // Initializes the signup button
    private void initializeSignupButton() {
        Button signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText usernameET = findViewById(R.id.username_edit_text);
                EditText passwordET = findViewById(R.id.password_edit_text);
                loginSignup(usernameET.getText().toString(), passwordET.getText().toString(), false);
            }
        });
    }

    // Initializes the login button
    private void initializeLoginButton() {
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText usernameET = findViewById(R.id.username_edit_text);
                EditText passwordET = findViewById(R.id.password_edit_text);
                loginSignup(usernameET.getText().toString(), passwordET.getText().toString(), true);
            }
        });
    }

    // Attempts to either login or signup the user with the given username and password
    public void loginSignup(final String username, String password, final boolean login) {
        final TextView errorText = findViewById(R.id.error_text);
        if (username.equals("")) {
            errorText.setText("Username cannot be empty");
        } else if (username.length() > 14) {
            errorText.setText("Username must be less than 14 characters");
        } else if (password.equals("")) {
            errorText.setText("Password cannot be empty");
        } else {
            String email = username + "@example.com";
            Task<AuthResult> authResultTask;
            if (login) {
                authResultTask = mAuth.signInWithEmailAndPassword(email, password);
            } else {
                authResultTask = mAuth.createUserWithEmailAndPassword(email, password);
            }
            authResultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        currentUser = mAuth.getCurrentUser();
                        if (!login) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            currentUser.updateProfile(profileUpdates);
                        }
                        returnName = username;
                        exitActivity();
                    } else {
                        String error = "Unknown error";
                        if (task.getException() != null) error = task.getException().getMessage();
                        errorText.setText(error);
                    }
                }
            });
        }
    }

    // Exits the login activity returning the current user id
    public void exitActivity() {
        Intent returnData = new Intent();
        Bundle b = new Bundle();
        b.putString("user_id", returnName);
        returnData.putExtras(b);
        setResult(RESULT_OK, returnData);
        finish();
    }
}
