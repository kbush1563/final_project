package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

class SpaceShip extends PhysicalObject {
    private int currentRounds, health;
    private Point lastLoc;

    // Initialize class constants
    private final static int INITIAL_SPEED;
    private final static int INITIAL_HEALTH;
    static {
        INITIAL_SPEED = 20;
        INITIAL_HEALTH = 1000;
    }

    SpaceShip(int x, int y, Context c) {
        super(x, y, INITIAL_SPEED, INITIAL_SPEED, null, c);
        Bitmap b = Utils.drawableToBitmap(c.getResources().getDrawable(R.drawable.ic_ship, null));
        this.setImage(b);
        constructorHelper();
    }

    private void constructorHelper() {
        lastLoc = new Point(getX(), getY());
        health = INITIAL_HEALTH;
    }

    void setLastPos(Point p) {
        lastLoc = p;
    }

    @Override
    boolean update() {
        int xMod = getX();
        int yMod = getY();
        if (Math.abs(lastLoc.x - getCenteredX()) > getxVel()) {
            if (getCenteredX() > lastLoc.x) xMod -= getxVel();
            else if (getCenteredX() < lastLoc.x) xMod += getxVel();
        }
        int tgtY = getY() + getSY();
        if (Math.abs(lastLoc.y - tgtY) > getyVel()) {
            if (tgtY > lastLoc.y) yMod -= getyVel();
            else if (tgtY < lastLoc.y) yMod += getyVel();
        }
        setPosition(xMod, yMod);
        return false;
    }

    // Damages the players ship
    void damage(Type t) {
        switch (t) {
            case OCTO:
                health -= 1;
                break;
            case ASTEROID:
                health -= 75;
                break;
        }
        if (health < 0) health = 0;
    }

    boolean isAlive() {return health > 0;}

    @Override
    void drawCentered(Canvas canvas, Paint p) {
        // Draws the current amount of rounds left
        float x = getX() + getSX() + 10;
        float y = getY();
        p.setColor(Color.WHITE);
        p.setTextSize(50);
        p.setAntiAlias(true);
        canvas.drawText(currentRounds + "", x, y, p);
        // Draws the health bar
        p.setColor(Color.GREEN);
        int startY = (int) (getY() + getSY() - (getSY() * ((health * 1.0)/ INITIAL_HEALTH)));
        canvas.drawRect(getX() - 10, startY, getX(), getY() + getSY(), p);
        // Draws the main ship
        p.setColor(Color.BLACK);
        super.drawCentered(canvas, p);
    }

    void setCurrentRounds(int rnds) {currentRounds = rnds;}

    // TODO: add reload bar
}