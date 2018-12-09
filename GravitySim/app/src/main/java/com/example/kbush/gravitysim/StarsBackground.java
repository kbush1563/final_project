package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;

import java.util.LinkedList;
import java.util.Random;

public class StarsBackground {
    private final static int NUMBER_OF_STARS = 10;
    private LinkedList<PhysicalObject> stars;
    private GameView graphics;
    private Context context;
    GameBackend gameBackend;
    Runnable runnable;
    Handler handler = new Handler();
    boolean started;

    StarsBackground(GameView givenView, Context givenContext) {
        graphics = givenView;
        context = givenContext;

        stars = new LinkedList<>();
        for (int i = 0; i < NUMBER_OF_STARS; i++) {
            int x = new Random().nextInt(Utils.getScreenWidth());
            int y = new Random().nextInt(Utils.getScreenHeight());
            int yVel = new Random().nextInt(35) + 40;
            stars.add(new Projectile(new Point(x, y), 0, yVel, context, Type.STAR));
        }
        updateStars();
        start();
    }

    // Sets up the background stars
    void updateStars() {
        // Setup game timer
        runnable = new Runnable() {
            @Override
            public void run() {
                if (stars != null) {
                    for (PhysicalObject star : stars) {
                        if (star.update()) star.setPosition(-123456, 0);
                    }
                    graphics.setObjects(stars, null);
                    graphics.invalidate();
                }
                if (started) start();
            }
        };
    }

    // Stops the game timer
    public void kill() {
        started = false;
        handler.removeCallbacks(runnable);
        stars = null;
    }

    // Starts the game timer
    public void start() {
        started = true;
        handler.postDelayed(runnable, 1);
    }
}
