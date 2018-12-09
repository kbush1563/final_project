package com.example.kbush.gravitysim;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class SettingsActivity extends AppCompatActivity {
    FirebaseFirestore database;
    String uid;
    StarsBackground starBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        uid = b.getString("uid");

        // Initialize variables
        GameView starsBackground = findViewById(R.id.settings_stars);
        starBg = new StarsBackground(starsBackground, getApplicationContext());
        database = FirebaseFirestore.getInstance();

        // Initialize buttons
        initializeBackButton();
        initializeLogoutButton();
    }

    // Initializes the back button
    void initializeBackButton() {
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitActivity(false);
            }
        });
    }

    // Initializes the logout button
    void initializeLogoutButton() {
        Button logoutButton = findViewById(R.id.button_log_out);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitActivity(true);
            }
        });
    }

    // Exits the activity
    void exitActivity(boolean shouldLogout) {
        Bundle b = new Bundle();
        b.putBoolean("logout", shouldLogout);
        Map<String, Object> settings = updateUserSettings();
        Intent exitData = new Intent();
        exitData.putExtras(b);

        starBg.kill();
        setResult(RESULT_OK, exitData);
        finish();
    }


    // Looks through all of the settings options and then updates them on the database
    Map<String, Object> updateUserSettings() {
        Map<String, Object> userData = new HashMap<>();
        // Sfx settings
        CheckBox sfx = findViewById(R.id.cb_sfx_off);
        userData.put("sfxOff", sfx.isChecked());
        // Music settings
        CheckBox music = findViewById(R.id.cb_music_off);
        userData.put("musicOff", music.isChecked());

        database.collection("users").document(uid).set(userData, SetOptions.merge());
        return userData;
    }

}
