package org.epoint.avionics.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by nagydani on 2015.08.31..
 */
public class Gauge {

    private static final Paint scalePaint = new Paint();
    private static final float SCALE_WIDTH = 0.1f;
    private static final float ANGLE = 120f;

    static {
        scalePaint.setStrokeCap(Paint.Cap.BUTT);
        scalePaint.setStyle(Paint.Style.STROKE);
        scalePaint.setTextAlign(Paint.Align.CENTER);
        scalePaint.setTextSize(26);
    }

    public static void draw(Canvas c, float x, float y, float w, float h) {
        float scaleWidth, r, y2;
        if(w > 2 * h) {
            w = h * 2;
        } else {
            h = w / 2;
        }
        r = 0.9f * h; y2 = y + 0.5f * h;
        scalePaint.setStrokeWidth(scaleWidth = SCALE_WIDTH * h);
        scalePaint.setColor(Color.RED);
        RectF rectF = new RectF(x - r, y2 - r, x + r, y2 + r);
        c.drawArc(rectF, 270f - 0.5f * ANGLE, ANGLE, false, scalePaint);
        scalePaint.setColor(Color.WHITE);
        Compass.draw(c, scalePaint, 0, 0, x, y, x > y ? y * 0.8f : x * 0.8f);
    }
}
