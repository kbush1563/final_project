package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.DisplayMetrics;

import java.util.Random;

public abstract class PhysicalObject {
    private Point loc;
    private int xVel, yVel;
    private Bitmap objectImage;
    private Context context;
    private Type objectType;

    PhysicalObject(int x, int y, int x2, int y2, Bitmap img, Context c) {
        context = c;
        loc = new Point(x, y);
        objectImage = img;
        xVel = x2;
        yVel = y2;
        objectType = null;
    }

    void setObjectType(Type t) {objectType = t;}

    Type getObjectType() {return objectType;}

    // Sets the current position of the projectile
    void setPosition(int x, int y) {
        loc.set(x, y);
        if (x == -123456) loc.x = new Random().nextInt(Utils.getScreenWidth() +
                objectImage.getWidth()) - objectImage.getWidth();
        if (y == -123456) loc.y = new Random().nextInt(Utils.getScreenHeight());
    }

    // Updates the position of the projectile, if it is off screen
    // then it will return true.
    boolean update() {
        loc.y += yVel;
        loc.x += xVel;
        return !inScreen();
    }

    // Returns true if the projectile is still in the screen
    public boolean inScreen() {
        // Copied from stack overflow: https://stackoverflow.com/questions/4743116/get-screen-width-and-height
        int w = Utils.getScreenWidth();
        int h = Utils.getScreenHeight();
        return inBounds(loc, -getSX(), w, 0, h);
    }

    private boolean inBounds(Point p, int xStart, int xEnd, int yStart, int yEnd) {
        return (p.x > xStart) && (p.x < xEnd) && (p.y > yStart) && (p.y < yEnd);
    }

    // Draws the projectile
    void drawObject(Canvas canvas, Paint p) {
        p.setColor(Color.BLACK);
        canvas.drawBitmap(objectImage, loc.x, loc.y, p);
    }

    void drawCentered(Canvas canvas, Paint p) {
        canvas.drawBitmap(objectImage, loc.x, loc.y, p);
        Paint stroke = new Paint();
        stroke.setStyle(Paint.Style.STROKE);
    }

    // Returns the x position of the projectile
    int getX() {return loc.x;}

    // Returns the y position of the projectile
    int getY() {return loc.y;}

    Point getLoc() {return loc;}

    // Returns the width of the projectile
    int getSX() {return objectImage.getWidth();}

    // Returns the height of the projectile
    int getSY() {return objectImage.getHeight();}

    // Returns the  xVelocity of the object
    int getxVel() {return xVel;}

    // Returns the y velocity of the object
    int getyVel() {return yVel;}

    int getCenteredX() {return loc.x + (getSX() / 2);}

    int getCenteredY() {return loc.y + (getSY() / 2);}

    void setImage(Bitmap b) {objectImage = b;}


    // Returns true if a projectiles are overlapping
    boolean isOverProjectile(PhysicalObject p) {
        if (p.getSX() > objectImage.getWidth() && p.getSY() > objectImage.getHeight())
            return collides(loc.x, loc.y, objectImage.getWidth(), objectImage.getWidth(),
                    p.getX(), p.getY(), p.getSX(), p.getSY());
        else
            return collides(p.getX(), p.getY(), p.getSX(), p.getSY(), loc.x, loc.y, objectImage.getWidth(), objectImage.getHeight());
    }

    // First x1 should be smaller
    private boolean collides(int x1, int y1, int sx1, int sy1, int x2, int y2, int sx2, int sy2) {
        return collideHelper(x1, y1, x2, y2, sx2, sy2) ||
                collideHelper(x1, y1 + sy1, x2, y2, sx2, sy2) ||
                collideHelper(x1 + sx1, y1, x2, y2, sx2, sy2) ||
                collideHelper(x1 + sx1, y1 + sy1, x2, y2, sx2, sy2);
    }

    private boolean collideHelper(int x1, int y1, int x2, int y2, int sx, int sy) {
        return (x1 > x2 && x1 < x2 + sx && y1 > y2 && y1 < y2 + sy);
    }

    public void setRandomXVel(int start, int end) {
        if (start < 0) end -= start;
        xVel = new Random().nextInt(end) + start;
        if (xVel == 0) xVel += 1;
    }

    public void setRandomYVel(int start, int end) {
        if (start < 0) end -= start;
        yVel = new Random().nextInt(end) + start;
        if (yVel == 0) yVel += 1;
    }

    public void spawnAt(Spawn area, int margin) {
        int x = 0;
        int y = 0;
        Random r = new Random();

        Spawn[] randList = {Spawn.LEFT, Spawn.RIGHT, Spawn.BOTTOM};
        if (area == Spawn.SIDES) {
            area = randList[r.nextInt(2)];
        } else if (area == Spawn.UMBRELLA) {
            area = randList[r.nextInt(3)];
        }

        switch (area) {
            case BOTTOM:
                y = Utils.getScreenHeight() - margin;
            case TOP:
                y += r.nextInt(margin);
                x = r.nextInt(Utils.getScreenWidth() + objectImage.getWidth())
                        - objectImage.getWidth();
                break;
            case RIGHT:
                x = Utils.getScreenWidth() - margin;
            case LEFT:
                x += r.nextInt(margin);
                y = r.nextInt(Utils.getScreenHeight());
                break;
        }
        loc = new Point(x, y);

    }
}

// An enumeration for where a physical object should spawn
enum Spawn {
    TOP, LEFT, RIGHT, BOTTOM, SIDES, UMBRELLA
}