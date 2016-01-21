package org.epoint.avionics.view;

import org.epoint.avionics.Units;
import org.epoint.avionics.Units.*;
import org.epoint.avionics.math.Vec3D;
import org.epoint.avionics.math.Versor;
import org.epoint.avionics.sensor.Barometric;
import org.epoint.avionics.sensor.Inertial;

/**
 * Inertial and Barometric instrument model
 * @author <a href="mailto:nagydani@epoint.org">Daniel A. Nagy</a>
 */
public class InertialVisualization implements Barometric.Listener, Inertial.Listener {
    /**
     * Debug information displayed in the corner
     */
    public volatile String debug = null;
    /**
     * Unit vector pointing upwards
     */
    public volatile Vec3D up = new Vec3D(1);
    /**
     * Direction of movement in degrees (geographic)
     */
    public volatile float bearing = 0;
    /**
     * Heading direction in degrees (geographic)
     */
    public volatile float heading = 0;
    /**
     * Magnetic declination at present location
     */
    public volatile float declination = 0;
    /**
     * Altitude in meters
     */
    public volatile float altitude = 0;
    /**
     * Altitude display units
     */
    public Units.Altitude altitudeUnit = Altitude.FT;
    /**
     * Vertical speed in meters per second
     */
    public volatile float verticalSpeed = 0;
    /**
     * Vertical speed display units
     */
    public Units.VSpeed verticalSpeedUnit = VSpeed.FPM;
    /**
     * Lateral acceleration in meter per second squared
     */
    public volatile float slip = 0;
    /**
     * Rate of turn in radians per second
     */
    public volatile float rateOfTurn = 0;

    @Override
    public void setAltitude(float a) {
        altitude = a;
    }

    @Override
    public void setVerticalSpeed(float v) {
        verticalSpeed = v;
    }

    @Override
    public void setDebug(String d) {
        debug = d;
    }

    @Override
    public synchronized void setOrientation(Versor aircraftToWorld) {
        up = aircraftToWorld.rot(1);
        float h = (float) -aircraftToWorld.inv().yaw();
        bearing += h - heading;
        if(bearing > 360) bearing -= 360;
        else if(bearing < 0) bearing += 360;
        heading = h;
    }

    @Override
    public synchronized void setBearing(float b) {
        bearing = b;
    }

    @Override
    public void setDeclination(float d) {
        declination = d;
    }

    @Override
    public void setSlip(float s) {
        slip = s;
    }

    @Override
    public void setRateOfTurn(float r) {
        rateOfTurn = r;
    }
}
