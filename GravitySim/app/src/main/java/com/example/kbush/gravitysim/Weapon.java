package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Point;

class Weapon {
    private int capacity, fireRate, velocity, currentRounds;
    private Context context;
    private Type type;
    private long lastRequest;

    private final static int BASE_BULLET_RATE = 500;
    private final static int BASE_BULLET_CAPACITY = 1;
    private final static int BASE_LASER_RATE = 1000;
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
            return new Projectile(p, 0, velocity, context, type);
        } else return null;
    }

    void reload() {
        if (type == Type.BULLET) {
            if (currentRounds != 0) lastRequest = System.currentTimeMillis();
            if (System.currentTimeMillis() - lastRequest > fireRate) currentRounds = capacity;
        } else if (System.currentTimeMillis() - lastRequest > fireRate) {
            if (currentRounds < capacity) {
                currentRounds++;
                lastRequest = System.currentTimeMillis();
            }
        }
    }

    int getCurrentRounds() {
        return currentRounds;
    }
}