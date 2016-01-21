package org.epoint.avionics;

/**
 * Created by nagydani on 2015.09.11..
 */
public class Units {

    public enum Altitude {
        M(1, "m", 10), FT(3.28084f, "ft", 50);

        public final float inMeter;
        public final String abbr;
        public final float tick;

        private Altitude(float m, String a, float t) {
            inMeter = m;
            abbr = a;
            tick = t;
        }

        public String toString(float metric) {
            return String.format("%d", Math.floor(0.5 + inMeter * metric));
        }
    };

    public enum HSpeed {
        MPS(1, "m/s", 5), KPH(3.6f,"km/h", 10), KTS(1.94384449f, "kt", 10),
        MPH(2.23694f, "mph", 10), MACH(2.93858e-3f, "M", 0.1f);

        public final float inMPS;
        public final String abbr;
        public final float tick;

        private HSpeed(float m, String a, float t) {
            inMPS = m;
            abbr = a;
            tick = t;
        }

        public String toString(float metric) {
            switch(this) {
                case MACH:
                    return String.format("%0.1f", inMPS * metric);
                default:
                    return String.format("%d", Math.floor(0.5 + inMPS * metric));
            }
        }
    };

    public enum VSpeed {
        MPS(1, "m/s", 1), FPM(0.00508f,"ft/m", 100);

        public final float inMPS;
        public final String abbr;
        public final float tick;

        private VSpeed(float m, String a, float t) {
            inMPS = m;
            abbr = a;
            tick = t;
        }

        public String toString(float metric) {
            switch(this) {
                case MPS:
                    return String.format("%0.1f", metric);
                default:
                    return String.format("%d", Math.floor(0.5 + inMPS * metric));
            }
        }
    };

    public enum Distance {
        M(1, "m", 100), FT(3.28084f, "ft", 500),
        KM(0.001f, "km", 1), NM(5.399568e-4f, "nm", 1), MI(6.21371e-4f, "mi", 1);

        public final float inMeter;
        public final String abbr;
        public final float tick;

        private Distance (float m, String a, float t) {
            inMeter = m;
            abbr = a;
            tick = t;
        }

        public String toString(float metric) {
            switch(this) {
                case M:
                case FT:
                    return String.format("%d", Math.floor(0.5 + inMeter * metric));
                default:
                    return String.format("%0.1f", metric);
            }
        }
    };

    public enum Pressure {
        HPA(1, "hPa", 1, "%d"), INHG(33.8653f, "inHg", 0.02f, "%0.2f"), MMHG(1.333223f, "mmHg", 1, "%d");

        public final float inHPa;
        public final String abbr;
        public final float incr;
        private final String fmt;

        private Pressure(float hpa, String a, float i, String f) {
            inHPa = hpa; abbr = a; incr = i; fmt = f;
        }

        public String toString(float hpa) {
            return String.format(fmt, hpa / inHPa);
        }
    }
}

