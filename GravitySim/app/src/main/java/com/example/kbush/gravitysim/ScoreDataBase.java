package com.example.kbush.gravitysim;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class ScoreDataBase {
    // Initialize class constants
    private final static int NUMBER_OF_HIGHSCORES;
    static {
        NUMBER_OF_HIGHSCORES = 100;
    }

    private FirebaseFirestore db;
    private String uid;
    private int highScore;
    private ArrayList<HighScoreReference> topScoreList;

    ScoreDataBase(String uid) {
        db = FirebaseFirestore.getInstance();
        Settings.database = db;
        this.uid = uid;
        topScoreList = new ArrayList<>();
    }

    // Pulls the high score for the user from the database. If there is none, the default is 0
    void getUserScore(final TextView scoreText) {
        final DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("DatabasePull", "DocumentSnapshot data: " + document.getData());
                        try {
                            highScore = Integer.parseInt(document.getString("score"));
                        } catch (Exception e) {
                            highScore = 0;
                        }
                    } else {
                        Log.d("DatabasePull", "No such document");
                        highScore = 0;
                    }
                    String scoreValueText = "BEST: " + highScore;
                    scoreText.setText(scoreValueText);
                } else {
                    Log.d("DatabasePull", "get failed with ", task.getException());
                }
            }
        });
    }

    // Given a score, if it is larger than the highscore, update the database and set the textview
    void updateUserScore(final TextView scoreText, final int score) {
        if (score > highScore) {
            highScore = score;
            // Set value in database
            Map<String, Object> userData = new HashMap<>();
            userData.put("score", (score + ""));
            db.collection("users").document(uid)
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("DatabaseSet", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("DatabaseSet", "Error writing document", e);
                        }
                    });
        }
        String highScoreText = "BEST: " + highScore;
        scoreText.setText(highScoreText);
    }

    // Pulls a list of the top 1000 scores from the leaderboard
    void pullTopScores(final ListView highScoreView, final Context context) {
        DocumentReference docRef = db.collection("highscores").document("scores");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        for (int i = 0; i < NUMBER_OF_HIGHSCORES; i++) {
                            String name = document.getString("n" + i);
                            String score = document.getString("v" + i);
                            topScoreList.add(new HighScoreReference(name, score));
                        }
                        setHighscoreList(context, highScoreView);
                    } else Log.d("DatabasePull", "No such document");
                } else {
                    Log.d("DatabasePull", "get failed with ", task.getException());
                }
            }
        });
    }

    void updateHighScore(Context context, ListView givenView) {
        if (topScoreList.size() == NUMBER_OF_HIGHSCORES) {
            setNewListData();
            setHighscoreList(context, givenView);
        }
    }

    private void setHighscoreList(Context context, ListView highScoreView) {
        String[] scoreStrings = new String[topScoreList.size()];
        for (int i = 0; i < scoreStrings.length; i++) {
            scoreStrings[i] = (i + 1) + ". " + topScoreList.get(i).toString();
        }
        ListAdapter theAdaptor = new ArrayAdapter<>(context, R.layout.text_centered, R.id.layoutA, scoreStrings);
        highScoreView.setAdapter(theAdaptor);
    }

    void insertScoreOnly(int score) {
        HighScoreReference result = new HighScoreReference(uid, (score + ""));
        int index = topScoreList.size();
        if (index > 0 && score > topScoreList.get(index - 1).getScore()) {
            while (index > 0 && score > topScoreList.get(index - 1).getScore()) {
                index--;
            }
            topScoreList.add(index, result);
            topScoreList.remove(topScoreList.size() - 1);
            setNewListData();
        }
    }

    // Sets the highscore list on the database
    private void setNewListData() {
        Map<String, Object> userData = new HashMap<>();
        for (int i = 0; i < NUMBER_OF_HIGHSCORES; i++) {
            topScoreList.get(i).putData(userData, i);
        }
        db.collection("highscores").document("scores").set(userData);
    }

    /*
    WARNING: RESETS ALL HIGH SCORE DATA
    void setInitialData() {
        Map<String, Object> userData = new HashMap<>();
        for (int i = 0; i < NUMBER_OF_HIGHSCORES; i++) {
            userData.put("v" + i, "0");
            userData.put("n" + i, "None");
        }
        db.collection("highscores").document("scores").set(userData);
    }
    */
}


// A class to represent a given high score
class HighScoreReference implements Comparable{
    private String name;
    private String score;
    private int scoreValue;

    HighScoreReference(String n, String s) {
        name = n;
        score = s;
        scoreValue = Integer.parseInt(s);
    }

    /*
    Map<String, Object> toDataPacket() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("score", score);
        return userData;
    }
    */

    int getScore() {
        return  scoreValue;
    }

    void putData(Map<String, Object> userData, int i) {
        userData.put("v" + i, score);
        userData.put("n" + i, name);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        try {
            HighScoreReference other = (HighScoreReference) o;
            return scoreValue - other.scoreValue;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return name + " - " + score;
    }
}