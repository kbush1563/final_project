package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.widget.TextView;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

class GameBackend {
    Context context;

    private GameView graphics;
    private SpaceShip player;
    private Weapon weapon;
    // Separate object list to improve efficiency
    private LinkedList<PhysicalObject> octos, bullets, stars, rocks, gibs;

    private MediaPlayer bgSong;
    private SoundPool sfx;

    private int score, currentScore, spawnRate;
    private int sfxDeadOcto, sfxGunshot, sfxOctoHit, sfxRockHit, sfxShipExplode, sfxBackgoundSong;
    private boolean sfxOn, gameOver;

    private final static int NUMBER_OF_STARS = 10;
    private final static int INITIAL_SPAWN_RATE = 50; // In ms
    private final static int OCTO_POINT_VALUE = 10;
    private final static int ROCK_POINT_VALUE = 20;
    private final static int TOUCH_FIRE_RADIUS = 500;

    GameBackend(GameView givenGameView, Context context) {
        this.context = context;
        graphics = givenGameView;

        invalidateSoundSettings();

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
        gameOver = false;

        sfx = new SoundPool(100, AudioManager.USE_DEFAULT_STREAM_TYPE, 0);
        sfxGunshot = sfx.load(context, R.raw.gunshot, 1);
        sfxDeadOcto = sfx.load(context, R.raw.dead_octo_1, 1);
        sfxOctoHit = sfx.load(context, R.raw.octo_hit_1, 1);
        sfxRockHit = sfx.load(context, R.raw.rock_hit_1, 1);
        sfxShipExplode = sfx.load(context, R.raw.ship_explode,1);
        sfxBackgoundSong = sfx.load(context, R.raw.super_dramatic, 1);
    }

    // GAME UPDATE FUNCTIONS #######################################################################

    // Updates and runs collision physics for all objects
    boolean updateObjects() {

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
                    playSfx(sfxDeadOcto);
                    break;
                }
            }
            if (!isDead && current.isOverProjectile(player)) {
                player.damage(Type.OCTO);
                //playSfx(sfxOctoHit);
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
                        playSfx(sfxRockHit);
                        break;
                    }
                }
            }
            if (!isDead && current.isOverProjectile(player)) {
                player.damage(Type.ASTEROID);
                destroyObject(Type.SHIP_GIB, current.getLoc(), 10);
                destroyObject(Type.ROCK_GIB, current.getLoc(), 4);
                playSfx(sfxRockHit);
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
            playSfx(sfxShipExplode);
            killBackgroundSong();
            gameOver = true;
        }
        return player.isAlive();
    }

    // Explodes an object into the number of gibs of it's type
    private void destroyObject(Type t, Point loc, int amt) {
        for (int i = 0; i < amt; i++) {
            Projectile gib = new Projectile(loc, 0, 0, context, t);
            gib.setRandomXVel(-20, 20);
            gib.setRandomYVel(-20, 20);
            gibs.add(gib);
        }

    }

    // Updates the score text one point at a time
    void updateScoreView(TextView scoreView) {
        if (currentScore < score) {
            currentScore++;
            String scoreViewText = "SCORE: " + currentScore;
            scoreView.setText(scoreViewText);
        }
    }

    // Combines all physical objects into one master list and passes it into the gameView
    void drawGraphics() {
        LinkedList<PhysicalObject> objects = new LinkedList<>(octos);
        objects.addAll(stars);
        objects.addAll(bullets);
        objects.addAll(rocks);
        objects.addAll(gibs);
        player.setCurrentRounds(weapon.getCurrentRounds());
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
                playSfx(sfxGunshot);
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

    // Toggles the sound settings based on the global settings class
    private void invalidateSoundSettings() {
        killBackgroundSong();
        if (!Settings.musicOff) {
            bgSong = MediaPlayer.create(context, R.raw.super_dramatic);
            bgSong.setLooping(true);
            bgSong.start();
        }
        sfxOn = !Settings.sfxOff;
    }

    void killBackgroundSong() {
        if (bgSong != null) {
            bgSong.stop();
            bgSong = null;
        }
    }

    void killSounds() {
        killBackgroundSong();
        sfxOn = false;
    }

    private void playSfx(int sfxId) {
        int volume = 1;
        if (sfxOn) sfx.play(sfxId, volume, volume, 0, 0, 1);
    }
}