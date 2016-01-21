package org.epoint.avionics.view;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Visualization of the rate of turn
 * @author <a href="mailto:nagydani@epoint.org">Daniel A. Nagy</a>
 */
public class TurnIndicator {
    /**
     * Standard ROT, 3 degrees per second, full turn in two minutes
     */
    private static final float TWO_MINUTE_TURN = (float)Math.toRadians(3);
    /**
     * Degrees to indicate standard ROT
     */
    private static final float ANGLE = 15;

    // Derived constants
    private static final float COS_ANGLE = (float)Math.cos(Math.toRadians(ANGLE));
    private static final float SIN_ANGLE = (float)Math.sin(Math.toRadians(ANGLE));
    private static final float SCALE = -ANGLE / TWO_MINUTE_TURN;

    public static void draw(Canvas c, Paint p, float rate, float x, float y, float r) {
        float rr = 0.8f * r;
        float[] mark = {
                x - r, y,
                x - rr, y,
                x + r, y,
                x + rr , y,
                x - COS_ANGLE * r, y + SIN_ANGLE * r,
                x - COS_ANGLE * rr, y + SIN_ANGLE * rr,
                x + COS_ANGLE * r, y + SIN_ANGLE * r,
                x + COS_ANGLE * rr, y + SIN_ANGLE * rr,
        };
        float[] symbol = {
                x + rr, y,
                x - rr, y,
                x, y,
                x, y - 0.1f * r
        };
        c.drawLines(mark, p);
        c.save();
        c.rotate(SCALE * rate, x, y);
        c.drawLines(symbol, p);
        c.restore();
    }
}
