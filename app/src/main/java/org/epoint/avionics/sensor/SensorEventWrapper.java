package org.epoint.avionics.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Wrap SensorEventListener to prevent concurrent execution of event processing.
 * Why on Earth should the application programmer do this?!
 */
public class SensorEventWrapper implements SensorEventListener {

    private final SensorEventListener sensorEventListener;

    private volatile int lock = 0;

    public SensorEventWrapper(SensorEventListener l) {
        sensorEventListener = l;
    }

    /**
     * Execute the wrapped listener's method only if the previous execution has already finished.
     * @param event passed to wrapped listener.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(lock++ > 0) return; // TAS
        sensorEventListener.onSensorChanged(event);
        lock = 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        sensorEventListener.onAccuracyChanged(sensor, accuracy);
    }
}
