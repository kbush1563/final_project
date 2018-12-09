package com.example.kbush.gravitysim;

import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


// ICON CREDIT
// https://www.freepik.com" title="Freepik
// https://www.flaticon.com/authors/roundicons
// https://www.flaticon.com/authors/smashicons

public class MainActivity extends AppCompatActivity {
    private String userId;
    private ScoreDataBase scoreDb;
    GameBackend gameBackend;
    Runnable runnable;
    Handler handler = new Handler();
    int gameTicks, endingScore;
    boolean started;

    final static int SETTINGS_RESULT = 1;
    final static int LOGIN_RESULT = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup utilities
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Utils.displayMetrics = displayMetrics;

        // Initialize variables
        endingScore = 0;
        startLoginActivity(false);
    }

    // Used for initializing the main screen for the first time
    public void initializeMainScreen() {
        scoreDb = new ScoreDataBase(userId);
        setupMainScreen();
        TextView userScore = findViewById(R.id.user_highscore_text);
        scoreDb.getUserScore(userScore);
        ListView highScoreView = findViewById(R.id.list_view);
        scoreDb.pullTopScores(highScoreView, getApplicationContext());
    }

    public void setupMainScreen() {
        setContentView(R.layout.activity_main);
        initializeSettingsButton();

        GameView background = findViewById(R.id.background_main);
        StarsBackground starsBg = new StarsBackground(background, getApplicationContext());

        // Set potential new high score
        TextView userScore = findViewById(R.id.user_highscore_text);
        scoreDb.updateUserScore(userScore, endingScore);
        ListView highScoreView = findViewById(R.id.list_view);
        scoreDb.insertNewHighScore(endingScore, getApplicationContext(), highScoreView);

        // Initialize variables
        started = false;

        // Setup start button
        Button start = findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.game_board);
                initiateGame();
            }
        });
    }


    // Initializes the game backend and starts the game timer
    public void initiateGame() {
        final GameView gameView = findViewById(R.id.gameBoard);
        gameBackend = new GameBackend(gameView, getApplicationContext());

        final Button replay = findViewById(R.id.restart_button);
        final Button mainMenu = findViewById(R.id.main_menu_button);
        final LinearLayout gameOverScreen = findViewById(R.id.game_over_layout);
        final ImageButton music = findViewById(R.id.music_button);
        gameTicks = 0;

        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                music.setVisibility(View.VISIBLE);
                gameOverScreen.setVisibility(View.GONE);
                TextView scoreView = findViewById(R.id.current_score_text);
                scoreView.setText("SCORE: 0");
                final GameView gameView = findViewById(R.id.gameBoard);
                gameBackend = new GameBackend(gameView, getApplicationContext());
            }
        });

        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
                setupMainScreen();
            }
        });

        // Setup game timer
        runnable = new Runnable() {
            @Override
            public void run() {
                gameTicks++;
                if (gameBackend.updateObjects()) {
                    TextView scoreView = findViewById(R.id.current_score_text);
                    gameBackend.updateScoreView(scoreView);
                    gameBackend.reload();
                    if (gameTicks >= gameBackend.spawnRate()) {
                        gameBackend.addOcto();
                        gameBackend.addRock();
                        gameTicks = 0;
                    }
                } else {
                    gameOverScreen.setVisibility(View.VISIBLE);
                    music.setVisibility(View.GONE);
                    endingScore = gameBackend.getScore();
                }
                gameBackend.drawGraphics();
                if (started) start();
            }
        };

        initiateMusicButton();
        start();
    }

    void initializeSettingsButton() {
        ImageButton settings = findViewById(R.id.button_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSettingsActivity();
            }
        });
    }

    void startSettingsActivity() {
        Intent theGame = new Intent(this, SettingsActivity.class);
        Bundle b = new Bundle();
        // TODO: Put stars settings?
        b.putString("uid", userId);
        theGame.putExtras(b);
        startActivityForResult(theGame, SETTINGS_RESULT);
    }

    void startLoginActivity(boolean logout) {
        Intent startData = new Intent(this, LoginActivity.class);
        Bundle b = new Bundle();
        // TODO: Put stars settings?
        b.putBoolean("logout", logout);
        startData.putExtras(b);
        startActivityForResult(startData, LOGIN_RESULT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle extras;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SETTINGS_RESULT:
                    extras = data.getExtras();
                    if (extras != null && extras.getBoolean("logout")) startLoginActivity(true);
                    break;
                case LOGIN_RESULT:
                    extras = data.getExtras();
                    if (extras != null) userId = extras.getString("user_id");
                    print("Welcome " + userId);
                    initializeMainScreen();
                    break;
            }
        }
    }

    // Initializes the music button
    void initiateMusicButton() {
        final ImageButton music = findViewById(R.id.music_button);
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameBackend.toggleMusic(music);
            }
        });
    }

    // When the user touches the screen, if they tap on the space ship it will fire a bullet,
    // otherwise it will move the space ship to the selected location.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!started) return false; // Do nothing on touch events if the game hasn't started
        Point touchLocation = new Point((int)event.getX(), (int)event.getY());
        boolean canFire = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                canFire = true;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
        }
        if (gameBackend.isOverPlayer(touchLocation) && canFire) {
            gameBackend.fire();
        } else gameBackend.movePlayerTo(touchLocation);
        return false;
    }

    // Stops the game timer
    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    // Starts the game timer
    public void start() {
        started = true;
        handler.postDelayed(runnable, 1);
    }

    private void print(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}