package com.example.kbush.gravitysim;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private StarsBackground starBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Initialize variables
        GameView starsBackground = findViewById(R.id.settings_stars);
        starBg = new StarsBackground(starsBackground, getApplicationContext());

        // Initialize buttons
        initializeBackButton();
        initializeLogoutButton();

        // Initialize Check Box Values
        CheckBox sfx = findViewById(R.id.cb_sfx_off);
        CheckBox music = findViewById(R.id.cb_music_off);
        sfx.setChecked(Settings.sfxOff);
        music.setChecked(Settings.musicOff);
    }

    @Override
    public void onBackPressed() {
        exitActivity(false);
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
        // Update user settings
        Map<String, Object> userData = new HashMap<>();
        CheckBox sfx = findViewById(R.id.cb_sfx_off);
        CheckBox music = findViewById(R.id.cb_music_off);
        userData.put("sfxOff", sfx.isChecked());
        userData.put("musicOff", music.isChecked());
        Settings.setUserSettings(userData);
        // Kill background
        starBg.kill();
        // Create return data package
        Intent returnData = new Intent();
        Bundle b = new Bundle();
        b.putBoolean("logout", shouldLogout);
        returnData.putExtras(b);
        setResult(RESULT_OK, returnData);
        finish();
    }
}
