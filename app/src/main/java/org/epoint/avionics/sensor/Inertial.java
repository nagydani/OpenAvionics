package org.epoint.avionics.sensor;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.epoint.avionics.math.Vec2D;
import org.epoint.avionics.math.Vec3D;
import org.epoint.avionics.math.Versor;

/**
 * Inertial navigation  augmented by satellite navigation
 * @author <a href="mailto:nagydani@epoint.org">Daniel A. Nagy</a>
 */
public class Inertial {
    /**
     * Maximum angular velocity drift for gyroscopes in rad/sec
     */
    private static final double MAX_ANGULAR = Math.toRadians(2);

    private static final double MAX_ANGULAR2 = MAX_ANGULAR * MAX_ANGULAR;

    /**
     * Fraction of the drift to be corrected in each calibration.
     */
    private static final double CALIBRATION_SCALE = 0.4;

    /**
     * Minimum required time in nanoseconds for calibration
     */
    private static final long STABLE_TIME = (long)2e9;

    /**
     * Minimal meaningful sepeed in meters per second
     */
    private static final float MIN_SPEED = 0.5f;

    /**
     * Maximal speed for guaranteed on-ground operation in meters per second
     */
    private static final float MAX_SPEED = 10f;

    public interface Listener {
        void setDebug(String debug);
        void setOrientation(Versor aircraftToWorld);
        void setBearing(float bearing);
        void setDeclination(float declination);
        void setSlip(float slip);
        void setRateOfTurn(float rateOfTurn);
    }

    private final Listener listener;

    public Inertial(Listener l) {
        listener = l;
    }

    /**
     * Last calibration at this time
     */
    long calibrationTimestamp = 0;

    private static class Damper implements SensorEventListener {
        /**
         * Filtering time in nanoseconds
         */
        private double time;
        /**
         * Timestamp of last sensor event
         */
        long lastTimestamp = 0;
        /**
         * Sensed vector, with damping
         */
        public Vec3D vector;
        /**
         * Variance, with damping
         */
        public double variance = 0;
        /**
         * Pass-through listener
         */
        private final SensorEventListener listener;
        /**
         * Stabilized vector
         */
        private Vec3D stableVector;
        /**
         * Stable since this timestamp
         */
        long firstStableTimestamp = 0;
        /**
         * Stabilization radius squared
         */
        private final double stableRadius2;

        /**
         * Create filtered sensor
         * @param t filtering time
         * @param r stabitity detection radius
         */
        public Damper(double t, double r) {this(t, r, null);}

        /**
         * Create filtered sensor with pass-through listener
         * @param t filtering time
         * @param r stabitity detection radius
         * @param l pass-through listener
         */
        public Damper(double t, double r, SensorEventListener l) {
            time = t;
            listener = l;
            stableRadius2 = r * r;
        }

        private void setStable() {
            stableVector = new Vec3D(vector);
            firstStableTimestamp = lastTimestamp;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            double t = (double)(event.timestamp - lastTimestamp) / time;
            Vec3D d = new Vec3D(event.values[0], event.values[1], event.values[2]);
            if(t >= 1 || vector == null) {
                vector = d;
                setStable();
            } else {
                d.sub(vector);
                double dv = d.dot(d) - variance;
                vector.add(d.scale(t));
                variance += dv * t;
            }
            lastTimestamp = event.timestamp;
            if(stableRadius2 > 0) {
                Vec3D r = new Vec3D(vector).sub(stableVector);
                if(r.dot(r) > stableRadius2)
                    setStable();
            } else setStable();
            if(listener != null) listener.onSensorChanged(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if(listener != null) listener.onAccuracyChanged(sensor, accuracy);
        }
    }

    private static final Vec3D GRAVITY = new Vec3D(0, SensorManager.STANDARD_GRAVITY, 0);
    private static final Vec3D NORTH = new Vec3D(0, 0, -1);
    Vec3D gravity = GRAVITY;
    Vec3D magnetism = new Vec3D(0, 0, -1);
    Vec3D gyroscopeDrift = Vec3D.zero();

    Versor deviceToAircraft = Versor.I;
    Versor deviceToWorld = null;

    public final Damper gyroscope = new Damper(5e8, 0, new SensorEventListener() {

        private long lastTimestamp = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(deviceToWorld == null) return;
            if(lastTimestamp != 0) {
                Vec3D angularVelocity = new Vec3D(event.values[0], event.values[1], event.values[2])
                        .sub(gyroscopeDrift);
                Versor spin = new Versor(angularVelocity ,
                        -1e-9 * (double) (event.timestamp - lastTimestamp));
                deviceToWorld = spin.mul(deviceToWorld);
                listener.setOrientation(deviceToWorld);
                if(gyroscope.vector != null)
                    listener.setRateOfTurn((float) deviceToWorld.rot(1).dot(gyroscope.vector));
            }
            lastTimestamp = event.timestamp;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    });

    private final SensorEventListener inclinometer = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(accelerometer.vector == null) return;
            if(event.timestamp - accelerometer.firstStableTimestamp > STABLE_TIME
                    && magnetometer.vector != null) {
                // calibration
                Versor d2w = Versor.matchingRotation(
                        accelerometer.vector, magnetometer.vector,
                        gravity, NORTH,
                        1);
                if(deviceToWorld == null) {
                    deviceToWorld = d2w;
                } else {
                    /**
                     * Reciprocal value of time in seconds since last recalibration, scaled
                     */
                    double dT = CALIBRATION_SCALE * 1e9 / (event.timestamp - calibrationTimestamp);
                    Vec3D measuredDrift = d2w.mul(deviceToWorld.inv()).rot().scale(dT);
                    measuredDrift.add(gyroscopeDrift);
                    if(measuredDrift.dot(measuredDrift) < MAX_ANGULAR2)
                        gyroscopeDrift = measuredDrift;
                    deviceToWorld = d2w;
                }
                accelerometer.firstStableTimestamp = calibrationTimestamp = event.timestamp;
            }
            listener.setSlip((float) accelerometer.vector.x());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public final Damper accelerometer = new Damper(2e8, 0.1, inclinometer);

    public final Damper magnetometer = new Damper(5e8, 0, null);

    public final LocationListener loc = new LocationListener() {

        private Vec2D lastVelocity;
        private long lastTimestamp = 0;

        private void setBearing(float bearing) {
            listener.setBearing(bearing < 0 ? 360f + bearing : bearing );
        }

        @Override
        public void onLocationChanged(Location location) {
            GeomagneticField g = new GeomagneticField(
                    (float) location.getLatitude(),
                    (float) location.getLongitude(),
                    (float) location.getAltitude(),
                    location.getTime());
            magnetism = new Vec3D(g.getY(), -g.getZ(), -g.getX()).unit();
            listener.setDeclination(g.getDeclination());
            if(location.hasBearing() && location.hasSpeed() && location.getSpeed() > MIN_SPEED ) {
                float bearing = location.getBearing();
                double b = Math.toRadians(bearing);
                setBearing(bearing);
                Vec2D velocity = new Vec2D(Math.sin(b), Math.cos(b));
                if(lastVelocity != null) {
                    lastVelocity.sub(velocity);
                    lastVelocity.scale(-1000.0 / (double) (location.getTime() - lastTimestamp));
                    gravity = new Vec3D(lastVelocity.x(), SensorManager.STANDARD_GRAVITY, -lastVelocity.y());
                } else {
                    gravity = GRAVITY;
                }
                lastVelocity = velocity;
            } else {
                if(lastVelocity != null) {
                    lastVelocity.scale(-1000.0 / (double) (location.getTime() - lastTimestamp));
                    gravity = new Vec3D(lastVelocity.x(), SensorManager.STANDARD_GRAVITY, -lastVelocity.y());
                } else {
                    gravity = GRAVITY;
                }
                lastVelocity = null;
            }
            lastTimestamp = location.getTime();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
