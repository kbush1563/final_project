package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class GameBackend {
    Context context;

    private GameView graphics;
    private SpaceShip player;
    private Weapon weapon;
    // Seprate object list to improve efficiency
    private LinkedList<PhysicalObject> octos, bullets, stars, rocks, gibs;

    private MediaPlayer bgSong;
    // TODO: SoundEngine sfx;

    private int score, currentScore, spawnRate;
    private boolean bgSongOn, sfxOn, gameOver;

    private final static int NUMBER_OF_STARS = 10;
    private final static int INITIAL_SPAWN_RATE = 100; // In ms
    private final static int OCTO_POINT_VALUE = 10;
    private final static int ROCK_POINT_VALUE = 20;
    private final static int TOUCH_FIRE_RADIUS = 500;

    GameBackend(GameView givenGameView, Context context) {
        this.context = context;
        graphics = givenGameView;

        player = new SpaceShip(Utils.getScreenWidth() / 2, Utils.getScreenHeight(), context);
        weapon = new Weapon(Type.BULLET, context);
        octos = new LinkedList<>();
        bullets = new LinkedList<>();
        // Setup Stars
        stars = new LinkedList<>();
        for (int i = 0; i < NUMBER_OF_STARS; i++) {
            int x = new Random().nextInt(Utils.getScreenWidth());
            int y = new Random().nextInt(Utils.getScreenHeight());
            int yVel = new Random().nextInt(35) + 40;
            stars.add(new Projectile(new Point(x, y), 0, yVel, context, Type.STAR));
        }
        rocks = new LinkedList<>();
        gibs = new LinkedList<>();

        score = 0;
        currentScore = 0;
        spawnRate = INITIAL_SPAWN_RATE;
        bgSongOn = false;
        sfxOn = false;
        gameOver = false;

        // Begin playing background music
        bgSong = MediaPlayer.create(context, R.raw.super_dramatic);
        bgSong.setLooping(true);
    }


    // GAME UPDATE FUNCTIONS #######################################################################

    // Updates and runs collision physics for all objects
    public boolean updateObjects() {
        // Update Stars
        for (PhysicalObject star: stars) {
            if (star.update()) star.setPosition(-123456, 0);
        }
        // Update Octos
        Iterator<PhysicalObject> octoItter =  octos.iterator();
        while (octoItter.hasNext()) {
            Octo current = (Octo) octoItter.next();
            current.setLastPos(player.getCenteredX(), player.getY() + player.getSY());
            current.update();
            boolean isDead = false;
            Iterator<PhysicalObject> bulletItter = bullets.iterator();
            while (bulletItter.hasNext()) {
                if (current.isOverProjectile(bulletItter.next())) {
                    bulletItter.remove();
                    octoItter.remove();
                    destroyObject(Type.DEAD, current.getLoc(), 6);
                    score += OCTO_POINT_VALUE;
                    isDead = true;
                    break;
                }
            }
            if (!isDead && current.isOverProjectile(player)) {
                player.damage(Type.OCTO);
            }
            if (gameOver) {
                octoItter.remove();
                destroyObject(Type.DEAD, current.getLoc(), 6);
            }

        }
        // Update asteroids
        Iterator<PhysicalObject> rockItter = rocks.iterator();
        while (rockItter.hasNext()) {
            boolean isDead = false;
            Projectile current = (Projectile) rockItter.next();
            if (current.update()) rockItter.remove();
            else {
                Iterator<PhysicalObject> bulletItter = bullets.iterator();
                while (bulletItter.hasNext()) {
                    if (current.isOverProjectile(bulletItter.next())) {
                        bulletItter.remove();
                        rockItter.remove();
                        destroyObject(Type.ROCK_GIB, current.getLoc(), 4);
                        score += ROCK_POINT_VALUE;
                        isDead = true;
                        break;
                    }
                }
            }
            if (!isDead && current.isOverProjectile(player)) {
                player.damage(Type.ASTEROID);
                destroyObject(Type.SHIP_GIB, current.getLoc(), 10);
                destroyObject(Type.ROCK_GIB, current.getLoc(), 4);
                rockItter.remove();
            }
            if (gameOver) {
                rockItter.remove();
                destroyObject(Type.ROCK_GIB, current.getLoc(), 4);
            }

        }
        Iterator<PhysicalObject> itter = bullets.iterator();
        while (itter.hasNext()) {
            PhysicalObject cur = itter.next();
            if (cur.update()) itter.remove();
        }
        itter = gibs.iterator();
        while (itter.hasNext()) {
            PhysicalObject cur = itter.next();
            if (cur.update()) itter.remove();
        }
        player.update();
        if (!player.isAlive() && !gameOver) {
            destroyObject(Type.SHIP_GIB, player.getLoc(), 200);
            gameOver = true;
        }
        return player.isAlive();
    }

    void destroyObject(Type t, Point loc, int amt) {
        for (int i = 0; i < amt; i++) {
            Projectile gib = new Projectile(loc, 0, 0, context, t);
            gib.setRandomXVel(-20, 20);
            gib.setRandomYVel(-20, 20);
            gibs.add(gib);
        }

    }

    void updateScoreView(TextView scoreView) {
        if (currentScore < score) {
            currentScore++;
            scoreView.setText("SCORE: " + currentScore);
        }
    }

    // Combines all physical objects into one master list and passes it into the gameView
    void drawGraphics() {
        LinkedList<PhysicalObject> objects = new LinkedList<>(octos);
        objects.addAll(stars);
        objects.addAll(bullets);
        objects.addAll(rocks);
        objects.addAll(gibs);
        player.setCurrentRounds(weapon.currentRounds);
        if (!gameOver) graphics.setObjects(objects, player);
        else graphics.setObjects(objects, null);
        graphics.invalidate();
    }

    // GAME ADD FUNCTIONS ##########################################################################
    int spawnRate() {return spawnRate;}

    // Spawns a Octo
    void addOcto() {
        Octo octo = new Octo(0, 0, 5, context);
        octo.spawnAt(Spawn.TOP, 1);
        octos.add(octo);
    }

    // Spawns an asteroid
    void addRock() {
        Projectile asteroid = new Projectile(context, Type.ASTEROID);
        asteroid.setRandomXVel(-10, 10);
        asteroid.setRandomYVel(5, 20);
        asteroid.spawnAt(Spawn.TOP, 1);
        rocks.add(asteroid);
    }

    // GAME REQUEST FUNCTIONS ######################################################################

    // Gets the players current score
    int getScore() {return score;}

    // PLAYER INTERACTION FUNCTIONS ################################################################

    // Fires a projectile from the players ship
    void fire() {
        if (player.isAlive()) {
            Point shipLoc = new Point(player.getCenteredX(), player.getCenteredY());
            Projectile bullet = weapon.fire(shipLoc);
            if (bullet != null) {
                bullets.add(bullet);
                //TODO: sfx.soundInstance(weapon.getType());
            }
        }
    }

    void reload() {
        weapon.reload();
    }

    // Returns whether a touch is over the players ship
    boolean isOverPlayer(Point touchLoc) {
        Double distance = Utils.distance(touchLoc, new Point(player.getCenteredX(), player.getCenteredY()));
        return distance < TOUCH_FIRE_RADIUS;
    }

    // Sets the move postition of the players ship
    void movePlayerTo(Point touchLoc) {
        if (player.isAlive()) player.setLastPos(touchLoc);
    }

    // SOUND FUNCTIONS #############################################################################

    // Toggles the background music on or off and sets the music icon
    void toggleMusic(ImageButton musicIC) {
        if (bgSongOn) {
            musicIC.setImageResource(R.drawable.ic_volume_off_black_24dp);
            bgSong.pause();
            bgSongOn = false;
        } else {
            musicIC.setImageResource(R.drawable.ic_music_on);
            bgSong.start();
            bgSongOn = true;
        }
    }
}