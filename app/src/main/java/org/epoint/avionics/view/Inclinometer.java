package org.epoint.avionics.view;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Slip indicator
 */
public class Inclinometer {

    private static final float SENSITIVITY = 0.8f;
    private static final float WIDTH = 7f;

    private static float limit(float x, float w) {
        return x < w ? x > -w ? x : -w : w;
    }

    /**
     * Draw inclinometer with ball
     * @param c target canvas
     * @param p scale paint
     * @param slip lateral acceleration in meters per second square, positive to the right
     * @param x center x coordinate
     * @param y center y coordinate
     * @param r ball radius
     */
    public static void draw(Canvas c, Paint p, float slip, float x, float y, float r) {
        float[] lines = {
                x - r, y - r,
                x - r, y + r,
                x + r, y - r,
                x + r, y + r
        };
        c.drawLines(lines, p);
        c.drawCircle(x - limit(SENSITIVITY * slip, WIDTH) * r, y, r, p);
    }
}
