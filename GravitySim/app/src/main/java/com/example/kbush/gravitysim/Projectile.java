package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import java.util.Random;

class Projectile extends PhysicalObject {
    Projectile(int x, int y, int x2, int y2, Bitmap img, Context c) {
        super(x, y, x2, y2, img, c);
    }

    Projectile(Point p, int x2, int y2, Context c, Type t) {
        this(p.x, p.y, x2, y2, null, c);
        int resource;
        boolean shouldRotate = false;
        switch (t) {
            case STAR:
                int[] stars = {
                //        R.drawable.ic_star_white,
                //        R.drawable.ic_star_red,
                //        R.drawable.ic_star_orange,
                        R.drawable.ic_star_yellow,
                //        R.drawable.ic_star_green,
                //        R.drawable.ic_star_blue,
                //        R.drawable.ic_star_purple
                };

                resource = stars[new Random().nextInt(stars.length)];
                shouldRotate = true;
                break;
            case SHIP:
                resource = R.drawable.ic_ship;
                break;
            case BULLET:
                resource = R.drawable.ic_bullet;
                break;
            case ASTEROID:
                resource = R.drawable.ic_asteroid1;
                shouldRotate = true;
                break;
            case DEAD:
                resource = R.drawable.ic_dead_octo;
                shouldRotate = true;
                break;
            case ROCK_GIB:
                resource = R.drawable.ic_rock;
                shouldRotate = true;
                break;
            case SHIP_GIB:
                int[] parts = {
                        R.drawable.ic_ship_screw,
                        R.drawable.ic_ship_nut,
                        R.drawable.ic_ship_gear
                };

                resource = parts[new Random().nextInt(parts.length)];
                shouldRotate = true;
                break;
            default:
                throw new IllegalArgumentException();
        }
        Bitmap b = Utils.drawableToBitmap(c.getResources().getDrawable(resource, null));
        if (shouldRotate) {
            float angle = new Random().nextInt(360);
            b = Utils.RotateBitmap(b, angle);
        }
        this.setImage(b);
        this.setObjectType(t);
    }

    Projectile(Context c, Type t) {
        this(new Point(0, 0), 0, 0, c, t);
    }
}