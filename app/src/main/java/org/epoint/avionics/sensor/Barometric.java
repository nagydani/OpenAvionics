package org.epoint.avionics.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by nagydani on 2015.09.19..
 */
public class Barometric implements SensorEventListener {

    public interface Listener {
        public void setAltitude(float altitude);
        public void setVerticalSpeed(float verticalSpeed);
    }

    /**
     * Scale height in meters for exponential atmospheric pressure model
     */
    static final float SCALE_HEIGHT = 8400f;

    /**
     * ISA sea-level pressure
     */
    static final float QNE = 1013.25f;

    /**
     * Maximal length of circular buffer
     */
    static final int BUFFER = 100;
    /**
     * Wrap-around time in nanoseconds
     */
    static final long TIME = 500000000; // 1/2 second

    float referencePressure = QNE;

    int index = 0;
    boolean hasVerticalSpeed = false;
    float cPressure;
    long lastTime = 0;
    float[] pressure = new float[BUFFER];
    long[] time = new long[BUFFER];
    private final Listener listener;

    public Barometric(Listener l) {
        listener = l;
    }

    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        if(lastTime == 0 || event.timestamp - lastTime > TIME) {
            cPressure = event.values[0];
        } else {
            float dT = (float)(event.timestamp - lastTime) / (float)TIME;
            cPressure *= dT;
            cPressure += (1.0f - dT) * event.values[0];
        }
        lastTime = time[index] = event.timestamp;
        float alt = getAltitude(cPressure);
        listener.setAltitude(alt);
        index++;
        if(index >= BUFFER || event.timestamp - time[0] > TIME) {
            index = 0;
            hasVerticalSpeed = true;
        }
        if(hasVerticalSpeed)
            listener.setVerticalSpeed(1e9f * (alt - getAltitude(pressure[index]))
                    / (float) (event.timestamp - time[index]));
    }

    private float getAltitude(float measuredPressure) {
        return SCALE_HEIGHT * (float)Math.log(referencePressure / measuredPressure);
    }

    public synchronized float setReferencePressure(float pressure) {
        index = 0;
        lastTime = 0;
        hasVerticalSpeed = false;
        return referencePressure = pressure;
    }

    private float setReferencePressure(float measuredPressure, float altitude) {
        return setReferencePressure(measuredPressure * (float)Math.exp(altitude / SCALE_HEIGHT));
    }

    public float setQFE() {
        return setReferencePressure(cPressure);
    }

    public float setQNH(float altitude) {
        return setReferencePressure(cPressure, altitude);
    }

    public float setQNE() {
        return setReferencePressure(QNE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
