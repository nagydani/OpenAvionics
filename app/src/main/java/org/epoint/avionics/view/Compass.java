package org.epoint.avionics.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Full-circle compass for heading and bearing indication
 * @author <a href="mailto:nagydani@epoint.org">Daniel A. Nagy</a>
 */
public class Compass {

    public static final float TICK_RADIUS = 0.97f;
    public static final float PLANE_RADIUS = 0.02f;
    public static final float MARK_RADIUS = 1.05f;
    public static final float MARK_WIDTH = 0.05f;

    private static final String[] LABEL = {
            "N", "01", "02", "03", "04", "05", "06", "07", "08",
            "E", "10", "11", "12", "13", "14", "15", "16", "17",
            "S", "19", "20", "21", "22", "23", "24", "25", "26",
            "W", "28", "29", "30", "31", "32", "33", "34", "35"
    };

    private static final float[] PLANE = {
            0, -3, 1, -1, 5, -1, 5, 1, 1, 1, 0.5f, 4, 2, 4, 2, 5,
            -2, 5, -2, 4, -0.5f, 4, -1, 1, -5, 1, -5, -1, -1, -1
    };

    private static final Path PLANE_PATH = new Path();

    static {
        PLANE_PATH.moveTo(PLANE[0], PLANE[1]);
        for(int i = 2; i < PLANE.length; i += 2) {
            PLANE_PATH.lineTo(PLANE[i], PLANE[i + 1]);
        }
        PLANE_PATH.close();
    }

    public static void draw(Canvas c, Paint p, float bearing, float heading, float x, float y, float r) {
        float r1 = y - r, r2 = y - TICK_RADIUS * r, r3 = r2 - p.getFontMetrics().top, r4 = r * PLANE_RADIUS;
        float[] mark = {
                x - MARK_WIDTH * r, y - r * MARK_RADIUS,
                x, y - r,
                x, y - r,
                x + MARK_WIDTH * r, y - r * MARK_RADIUS
        };
        c.drawCircle(x, y, r, p);
        c.save();
        c.translate(x, y);
        c.scale(r4, r4);
        c.drawPath(PLANE_PATH, p);
        c.restore();
        c.save();
        Paint.Style saveStyle = p.getStyle();
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        c.rotate(bearing - heading, x, y);
        c.drawText(String.format(" %03d°", (int) Math.floor(0.5 + bearing)),
                x, y - r * MARK_RADIUS - p.getFontMetrics().bottom, p);
        c.drawLines(mark, p);
        c.rotate(-bearing, x, y);
        for(int i = 0; i < 36; i += 1) {
            c.drawLine(x, r1, x, r2, p);
            c.drawText(LABEL[i],x, r3, p);
            c.rotate(10, x, y);
        }
        p.setStyle(saveStyle);
        c.restore();
    }
}
