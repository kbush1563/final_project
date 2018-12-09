package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Bitmap;
import java.util.Random;

public class Octo extends PhysicalObject {
    private int lastX;
    private int lastY;
    private final static int JITTER = 10;

    Octo(int x, int y, int x2, int y2, Bitmap img, Context c) {
        super(x, y, x2, y2, img, c);
    }

    Octo(int x, int y, int speed, Context c) {
        super(x, y, speed, speed, null, c);
        Bitmap b = Utils.drawableToBitmap(c.getResources().getDrawable(R.drawable.ic_octopus, null));
        this.setImage(b);
    }

    // Sets the last position of the player ship
    void setLastPos(int x, int y) {
        lastX = x;
        lastY = y;
    }

    // Moves the Octo to the given player ship
    @Override
    boolean update() {
        int xMod = getX();
        int yMod = getY();
        int tgtY = getY() + getSY() + getSY();
        if (Math.abs(lastY - tgtY) > getyVel()) {
            if (tgtY > lastY) yMod -= getyVel();
            else if (tgtY < lastY) yMod += getyVel();
        }
        if (Math.abs(lastX - getCenteredX()) > getxVel() && Math.abs(lastY - tgtY) < 500) {
            if (getCenteredX() > lastX) xMod -= getxVel();
            else if (getCenteredX() < lastX) xMod += getxVel();
        }
        if (new Random().nextBoolean()) xMod += JITTER;
        else xMod -= JITTER;
        if (new Random().nextBoolean()) yMod += JITTER;
        else yMod -= JITTER;

        setPosition(xMod, yMod);
        return false;
    }
}