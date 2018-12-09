package com.example.kbush.gravitysim;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

public class Settings {
    private final static boolean DEFAULT_SFX_OFF = false;
    private final static boolean DEFAULT_MUSIC_OFF = false;

    static boolean sfxOff;
    static boolean musicOff;
    static FirebaseFirestore database;
    static String uid;

    public static void setUserSettings(Map<String, Object> userData) {
        sfxOff = (boolean) userData.get("sfxOff");
        musicOff = (boolean) userData.get("musicOff");
        database.collection("users").document(uid).set(userData, SetOptions.merge());
    }

    public static void pullUserSettings() {
        DocumentReference docRef = database.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("DatabasePull", "DocumentSnapshot data: " + document.getData());
                        try {
                            sfxOff = document.getBoolean("sfxOff");
                            musicOff = document.getBoolean("musicOff");
                        } catch (Exception e) {
                            sfxOff = DEFAULT_SFX_OFF;
                            musicOff = DEFAULT_MUSIC_OFF;
                        }
                    } else {
                        Log.d("DatabasePull", "No such document");
                        sfxOff = DEFAULT_SFX_OFF;
                        musicOff = DEFAULT_MUSIC_OFF;
                    }
                } else {
                    Log.d("DatabasePull", "get failed with ", task.getException());
                }
            }
        });
    }

}
