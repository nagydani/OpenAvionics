package org.epoint.avionics.view;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import org.epoint.avionics.math.Vec2D;
import org.epoint.avionics.math.Vec3D;

/**
 * Artificial horizon with
 * @author <a href="mailto:nagydani@epoint.org">Daniel A. Nagy</a>
 */
public class Horizon {

    private static final int TICK = 5;
    private static final int PITCH_FOV = 20;
    private static final double SIN_TICK = Math.sin(Math.toRadians(TICK));
    private static final double COS_TICK = Math.cos(Math.toRadians(TICK));
    private static final Vec2D UP_TICK = new Vec2D(COS_TICK, SIN_TICK);
    private static final Vec2D DOWN_TICK = new Vec2D(COS_TICK, -SIN_TICK);

    private static void to(Path p, Vec2D v) {
        if(p.isEmpty())
            p.moveTo((float) v.x(), (float) v.y());
        else
            p.lineTo((float) v.x(), (float) v.y());
    }

    private static final Matrix m = new Matrix();

    private static void drawPitchMark(Canvas c, Paint p, int pitchMark, Vec2D pm, float ll, float l, float rr, float r, float x, float y, float d) {
        float pd = y - (float)(pm.x() * d / pm.y());
        if(pitchMark == 0) {
            c.drawLine(ll, pd,rr, pd, p);
        } else {
            float[] tick = {
                    ll, pd,
                    l, pd,
                    rr, pd,
                    r, pd
            };
            c.drawLines(tick, p);
            c.drawText("" + pitchMark, x, pd, p);
        }
    }

    public static void draw(Canvas c, Paint sky, Paint earth, Paint p, Vec3D up,
                            float left, float right, float top, float bottom, float y, float d) {
        float x = 0.5f * (left + right);
        Vec3D[] c3D= {
                new Vec3D(left - x, y - top, -d),
                new Vec3D(right - x, y - top, -d),
                new Vec3D(right - x, y - bottom, -d),
                new Vec3D(left - x, y - bottom, -d)
        };
        Vec2D[] c2D = {
                new Vec2D(left, top),
                new Vec2D(right, top),
                new Vec2D(right, bottom),
                new Vec2D(left, bottom)
        };
        Vec3D l3D = c3D[3];
        Vec2D l2D = new Vec2D(c2D[3]);
        Path path = new Path();
        double lDot = l3D.dot(up);
        for(int i = 0; i < c3D.length; i++) {
            double tDot = c3D[i].dot(up);
            if(lDot >= 0) {
                if(tDot < 0) {
                    to(path, l2D.scale(tDot).add(new Vec2D(c2D[i]).scale(-lDot)).scale(1.0/(tDot - lDot)));
                    to(path, c2D[i]);
                }
            } else {
                if(tDot < 0) {
                    to(path, c2D[i]);
                } else {
                    to(path, l2D.scale(tDot).add(new Vec2D(c2D[i]).scale(-lDot)).scale(1.0 / (tDot - lDot)));
                }
            }
            l3D = c3D[i];
            l2D = c2D[i];
            lDot = tDot;
        }
        path.close();
        c.drawPaint(sky);
        c.drawPath(path, earth);
        float ll = 0.3f * left + 0.7f * x, rr = 0.3f * right + 0.7f * x;
        float l = 0.1f * left + 0.9f * x, r = 0.1f * right + 0.9f * x, h = 0.02f * (right - left);
        float[] mark = {
                ll, y,
                l, y,
                l, y,
                l, y + h,
                rr, y,
                r, y,
                r, y,
                r, y + h
        };
        Vec2D u = new Vec2D(up.x(), up.y());
        double ul = u.abs();
        if(ul > SIN_TICK) { // prevent gimball lock
            c.save();
            Paint.Style save = p.getStyle();
            p.setStyle(Paint.Style.FILL_AND_STROKE);
            u.scale(1.0 / ul);
            m.setSinCos((float) u.x(), (float) u.y(), x, y);
            c.setMatrix(m);
            double pitch = Math.toDegrees(Math.atan2(up.z(), ul));
            int pitchMark = -TICK * (int)Math.floor(pitch / (double)TICK + 0.5);
            double pitchDiff = Math.toRadians(pitch + pitchMark);
            Vec2D pm = new Vec2D(Math.sin(pitchDiff), Math.cos(pitchDiff));
            drawPitchMark(c, p, pitchMark, pm, ll, l, rr, r, x, y, d);
            Vec2D pm1 = pm;
            for(int i = TICK; i < PITCH_FOV; i += TICK) {
                if(pitchMark - i < -90) break;
                pm1 = pm1.mul(UP_TICK);
                drawPitchMark(c, p, pitchMark - i, pm1, ll, l, rr, r, x, y, d);
            }
            pm1 = pm;
            for(int i = TICK; i < PITCH_FOV; i += TICK) {
                if(pitchMark + i > 90) break;
                pm1 = pm1.mul(DOWN_TICK);
                drawPitchMark(c, p, pitchMark + i, pm1, ll, l, rr, r, x, y, d);
            }
            p.setStyle(save);
            c.restore();
        }
        c.drawLines(mark, p);
        c.drawCircle(x, y, 1, p);
    }
}
