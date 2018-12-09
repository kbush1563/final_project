package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class SpaceShip extends PhysicalObject {
    private int speed, currentRounds, health;
    private Point lastLoc;

    private final static int INITIAL_SPEED = 20;
    private final static int INITIAL_HEALTH = 1000;

    SpaceShip(int x, int y, int x2, int y2, Bitmap img, Context c) {
        super(x, y, x2, y2, img, c);
        constructorHelper();
    }

    SpaceShip(int x, int y, Context c) {
        super(x, y, INITIAL_SPEED, INITIAL_SPEED, null, c);
        Bitmap b = Utils.drawableToBitmap(c.getResources().getDrawable(R.drawable.ic_ship, null));
        this.setImage(b);
        constructorHelper();
    }

    void constructorHelper() {
        speed = 1;
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
    boolean damage(Type t) {
        switch (t) {
            case OCTO:
                health -= 1;
                break;
            case ASTEROID:
                health -= 75;
                break;
        }
        if (health < 0) health = 0;
        return health == 0;
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
        p.setColor(Color.GREEN);
        int startY = (int) (getY() + getSY() - (getSY() * ((health * 1.0)/ INITIAL_HEALTH)));
        canvas.drawRect(getX() - 10, startY, getX(), getY() + getSY(), p);
        p.setColor(Color.BLACK);
        super.drawCentered(canvas, p);
    }

    void setCurrentRounds(int rnds) {currentRounds = rnds;};


    // TODO: add reload bar
}