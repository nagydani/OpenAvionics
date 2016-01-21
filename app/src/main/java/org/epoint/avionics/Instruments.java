package org.epoint.avionics;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.epoint.avionics.sensor.Inertial;
import org.epoint.avionics.sensor.SensorEventWrapper;
import org.epoint.avionics.view.InertialView;

/**
 * @author <a href="mailto:nagydani@epoint.org">Daniel A. Nagy</a>
 */
public class Instruments extends Activity {

    SensorManager sm;
    LocationManager lm;
    Sensor accelerometer;
    SensorEventListener acc;
    Sensor magnetometer;
    SensorEventListener mag;
    Sensor gyroscope;
    SensorEventListener gyr;
    LocationListener loc;
    InertialView iv;

    final static Criteria criteria = new Criteria();

    static {
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruments);
        iv = (InertialView) findViewById(R.id.inertial);
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        Inertial inertial = new Inertial(iv.getModel());
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acc = new SensorEventWrapper(inertial.accelerometer);
        magnetometer = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mag = new SensorEventWrapper(inertial.magnetometer);
        gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyr = new SensorEventWrapper(inertial.gyroscope);
        loc = inertial.loc;
    }

    private volatile GraphicsThread graphicsThread = null;

    private class GraphicsThread extends Thread {

        @Override
        public void run() {
            while (graphicsThread != null) {
                iv.drawStuff();
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    graphicsThread = null;
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized void stopGraphicsThread() {
        if (graphicsThread == null) return;
        Thread t = graphicsThread;
        graphicsThread = null;
        try {
            if (t.isAlive()) t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        if(lm != null)
            lm.removeUpdates(loc);
        if (gyroscope != null) sm.unregisterListener(gyr);
        if (accelerometer != null) sm.unregisterListener(acc);
        if (magnetometer != null) sm.unregisterListener(mag);
        stopGraphicsThread();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        stopGraphicsThread();
        graphicsThread = new GraphicsThread();
        graphicsThread.start();
        if (magnetometer != null)
            sm.registerListener(mag, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        if (accelerometer != null)
            sm.registerListener(acc, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        if (gyroscope != null)
            sm.registerListener(gyr, gyroscope, SensorManager. SENSOR_DELAY_FASTEST);
        if (lm != null)
            lm.requestLocationUpdates(lm.getBestProvider(criteria, true), 0, 0, loc);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_instruments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
