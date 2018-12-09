package com.example.kbush.gravitysim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import java.util.LinkedList;

public class GameView extends View {
    private LinkedList<PhysicalObject> objects;
    private SpaceShip ship;
    private Paint fill;

    public GameView(Context context) {
        super(context);
        constructorHelper();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        constructorHelper();
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructorHelper();
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructorHelper();
    }

    // Used by the constructors to initialize variables
    private void constructorHelper() {
        fill = new Paint();
        fill.setColor(Color.BLACK);
        objects = new LinkedList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        canvas.drawPaint(fill);
        for (PhysicalObject p : objects) {
            p.drawObject(canvas, fill);
        }
        if (ship != null) ship.drawCentered(canvas, fill);
    }

    // Sets the objects to draw for the game
    public void setObjects(LinkedList<PhysicalObject> obj, SpaceShip s) {
        objects = obj;
        ship = s;
    }
}
