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

import java.io.Serializable;

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

        Intent i = getIntent();
        Bundle b = i.getExtras();
        boolean logout = b.getBoolean("logout");
        if (logout) {
            currentUser = null;
        } else {
            // Login user
            currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                returnName = currentUser.getDisplayName();
                exitActivity();
            }
        }
    }

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

    public void loginSignup(final String username, String password, final boolean login) {
        final TextView errorText = findViewById(R.id.error_text);
        if (username.equals("")) {
            errorText.setText("Username cannot be empty");
        } else if (username.length() > 10) {
            errorText.setText("Username must be less than 10 characters");
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
                        String error = task.getException().getMessage();
                        errorText.setText(error);
                    }
                }
            });
        }
    }

    public void exitActivity() {
        Intent returnData = new Intent();
        Bundle b = new Bundle();
        b.putString("user_id", returnName);
        returnData.putExtras(b);
        setResult(RESULT_OK, returnData);
        finish();
    }
}
