package org.epoint.avionics.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.epoint.avionics.math.Vec3D;

/**
 * Created by nagydani on 2015.08.31..
 */
public class InertialView extends SurfaceView implements SurfaceHolder.Callback {

    private final SurfaceHolder holder;
    private final Paint debugPaint = new Paint();
    private final Paint scalePaint = new Paint();
    private final Paint earthPaint = new Paint();
    private final Paint skyPaint = new Paint();
    private static final float SCALE_WIDTH = 0.1f;
    private static final float ANGLE = 120f;

    public InertialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        debugPaint.setColor(Color.RED);
        debugPaint.setTextAlign(Paint.Align.LEFT);
        scalePaint.setStrokeCap(Paint.Cap.BUTT);
        scalePaint.setStyle(Paint.Style.STROKE);
        scalePaint.setTextAlign(Paint.Align.CENTER);
        scalePaint.setColor(Color.WHITE);
        scalePaint.setTextSize(26);
        scalePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        earthPaint.setStyle(Paint.Style.FILL);
        earthPaint.setColor(0xFF964B00); // brown color
        earthPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        skyPaint.setStyle(Paint.Style.FILL);
        skyPaint.setColor(Color.BLUE);
        holder = getHolder();
        holder.addCallback(this);
    }

    private final InertialVisualization iv = new InertialVisualization();

    public InertialVisualization getModel() {
        return iv;
    }

    public void drawStuff() {
        if(holder != null) surfaceCreated(holder);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas c = holder.lockCanvas(null);
        if(c == null) return;
        float w = c.getWidth(), h = c.getHeight(), x = w / 2, y = 0.75f * h, v = 0.25f * h;
        Horizon.draw(c, skyPaint, earthPaint, scalePaint, iv.up, 0, w, 0, h, v, w);
        Compass.draw(c, scalePaint, iv.bearing, iv.heading, x, y, 0.24f * h);
        Inclinometer.draw(c, scalePaint, iv.slip, x, y + 0.17f * h, 0.015f * h);
        TurnIndicator.draw(c, scalePaint, iv.rateOfTurn, x, y + 0.12f * h, 0.1f * h);
        if(iv.debug != null) {
            // DEBUG: just in case
            c.drawText(iv.debug, 0, debugPaint.getFontSpacing(), debugPaint);
        }
        holder.unlockCanvasAndPost(c);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
