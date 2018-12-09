package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.widget.Toast;

public class Weapon {
    int capacity, fireRate, velocity, currentRounds;
    Context context;
    Type type;
    long lastRequest;

    private final static int BASE_BULLET_RATE = 3000;
    private final static int BASE_BULLET_CAPACITY = 14;
    private final static int BASE_LASER_RATE = 300;
    private final static int BASE_LASER_CAPACITY = 1;
    private final static int BASE_MISSILE_RATE = 1000;
    private final static int BASE_MISSILE_CAPACITY = 2;

    Weapon(Type ammoType, Context context) {
        switch (ammoType) {
            case BULLET:
                capacity = BASE_BULLET_CAPACITY;
                fireRate = BASE_BULLET_RATE;
                break;
            case LASER:
                capacity = BASE_LASER_CAPACITY;
                fireRate = BASE_LASER_RATE;
                break;
            case MISSILE:
                capacity = BASE_MISSILE_CAPACITY;
                fireRate = BASE_MISSILE_RATE;
                break;
            default:
                throw new IllegalArgumentException();
        }
        type = ammoType;
        velocity = -30;
        lastRequest = 0;
        currentRounds = capacity;
        this.context = context;
    }

    Projectile fire(Point p) {
        if (currentRounds > 0) {
            currentRounds--;
            Projectile result = new Projectile(p, 0, velocity, context, type);
            return result;
        } else return null;
    }

    boolean reload() {
        if (type == Type.BULLET) {
            if (currentRounds != 0) {
                lastRequest = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastRequest > fireRate) {
                currentRounds = capacity;
                return true;
            }
        } else if (System.currentTimeMillis() - lastRequest > fireRate) {
            if (currentRounds < capacity) {
                currentRounds++;
                lastRequest = System.currentTimeMillis();
                return true;
            }
        }
        return false;
    }

    // Draws the current amount of rounds left
    void drawAmmoCount(Canvas c, SpaceShip ship, Paint paint) {
        float x = ship.getX() + ship.getSX() + 10;
        float y = ship.getY();
        paint.setColor(Color.parseColor("white"));
        c.drawText(currentRounds + "", x, y, paint);
    }

}