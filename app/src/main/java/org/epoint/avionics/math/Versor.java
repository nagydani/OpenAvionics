package org.epoint.avionics.math;

/**
 * Unit quaternion representing a rotation. Essentially immutable.
 */
public final class Versor extends Vec {

    /**
     * Identity.
     */
    public static final Versor I = new Versor();

    /**
     * Versor coordinates from angular velocity vector and time
     * @param r angular velocity vector
     * @param t time scalar
     * @return corresponsing Versor coordinates
     */
    private static double[] fromRotation(Vec3D r, double t) {
        double rabs = r.abs();
        if (rabs == 0.0) {
            return new double[] {1, 0, 0, 0 };
        }
        double a = 0.5 * rabs * t;
        double s = Math.sin(a) / rabs;
        return new double[] {Math.cos(a), r.x() * s, r.y() * s, r.z() * s };
    }

    /**
     * Versor coordinates from Tait-Bryan angles
     * @param roll Roll angle in degrees.
     * @param pitch Pitch angle in degrees.
     * @param yaw Yaw angle in degrees.
     * @return corresponding Versor coordinates
     */
    private static double[] fromRotation(double roll, double pitch, double yaw) {
        double sinRoll = Math.sin(0.5 * Math.toRadians(roll));
        double sinPitch = Math.sin(0.5 * Math.toRadians(pitch));
        double sinYaw = Math.sin(0.5 * Math.toRadians(yaw));
        double cosRoll = Math.cos(0.5 * Math.toRadians(roll));
        double cosPitch = Math.cos(0.5 * Math.toRadians(pitch));
        double cosYaw = Math.cos(0.5 * Math.toRadians(yaw));
        return new double[] {
                cosRoll * cosPitch * cosYaw + sinRoll * sinPitch * sinYaw,
                cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw,
                cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw,
                sinRoll * cosPitch * cosYaw - cosRoll * sinPitch * sinYaw};
    }

    /**
     * Versor coordinates from (orthonormal) rotational matrix
     * @param qx Basis vector x
     * @param qy Basis vector y
     * @param qz Basis vector z
     * @return corresponding Versor coordinates
     */
    private static double[] fromRotation(Vec3D qx, Vec3D qy, Vec3D qz) {
        double t = qx.x() + qy.y() + qz.z(); // trace
        double r = Math.sqrt(1.0 + t);
        return new double[] {
                0.5 * r,
                (qz.y() > qy.z() ? 0.5 : -0.5)
                * Math.sqrt(1 + qx.x() - qy.y() - qz.z()),
                (qx.z() > qz.x() ? 0.5 : -0.5)
                * Math.sqrt(1 - qx.x() + qy.y() - qz.z()),
                (qy.x() > qx.y() ? 0.5 : -0.5)
                * Math.sqrt(1 - qx.x() - qy.y() + qz.z()) };
    }

    /**
     * Construct versor from rotation vector.
     *
     * @param r angular velocity vector pointing in the axial direction
     * @param t time
     */
    public Versor(Vec3D r, double t) {
        super(fromRotation(r, t));
    }

    /**
     * Construct versor from Tait-Bryan angles.
     *
     * @param roll Rotation around longitudal axis (Z) in degrees
     * @param pitch Rotation around lateral axis (X) in degrees
     * @param yaw Rotation around vertical axis (Y) in degrees
     */
    public Versor(double roll, double pitch, double yaw) {
        super(fromRotation(roll, pitch, yaw));
    }

    /**
     * Construct versor from coordinates. Normalization included, array elements copied.
     * @param coordinates versor coordinates
     */
    public Versor(double[] coordinates) {
        super(new Vec(coordinates));
        if (coordinates.length != 4) {
            throw new IllegalArgumentException("Must have 4 coordinates");
        }
        unit();
    }

    /**
     * Copy constructor.
     *
     * @param o other Versor
     */
    public Versor(Versor o) {
        super(o);
    }

    /**
     * Real part of quaternion.
     *
     * @return real coordinate
     */
    public double r() {
        return coord[0];
    }

    /**
     * Imaginary part of quaternion.
     *
     * @return imaginary coordinates
     */
    public Vec3D i() {
        return new Vec3D(coord[1], coord[2], coord[3]);
    }

    /**
     * Construct versor from real and imaginary parts.
     *
     * @param r real part
     * @param i imaginary part
     */
    private Versor(double r, Vec3D i) {
        super(new double[] {r, i.x(), i.y(), i.z() });
    }

    /**
     * Identity constructor.
     */
    private Versor() {
        super(new double[] {1, 0, 0, 0 });
    }

    /**
     * Compose two rotations. Does not alter either.
     * @param o other rotation
     * @return the composition of <code>this</code> rotation with <code>o</code>
     */
    public Versor mul(Versor o) {
        Vec3D a = i();
        Vec3D b = o.i();
        Vec3D c = a.cross(b);
        Versor v = new Versor(r() * o.r() - i().dot(o.i()), a.scale(o.r())
                .add(b.scale(r())).add(c));
        v.renormalize();
        return v;
    }

    /**
     * Inverse rotation. Does not change original.
     * @return inverse of <code>this</code> rotation.
     */
    @Override
    public Versor inv() {
        return new Versor(r(), i().inv());
    }

    /**
     * Rotate a vector.
     *
     * @param v vector to be rotated.
     * @return <code>v</code> after update
     */
    public Vec3D rot(Vec3D v) {
        Vec3D i = i();
        return v.add(i.cross(i.cross(v).add(new Vec3D(v).scale(r())))
                .scale(2.0));
    }

    /**
     * Conversion to orthonormal 3D rotation matrix.
     *
     * @param i row index in the matrix (0, 1 or 2)
     * @return corresponding row in the matrix
     */
    public Vec3D rot(int i) {
        switch (i) {
        case 0:
            return new Vec3D(coord[0] * coord[0] + coord[1] * coord[1]
                    - coord[2] * coord[2] - coord[3] * coord[3],
                    2.0 * (coord[1] * coord[2] + coord[0] * coord[3]),
                    2.0 * (coord[1] * coord[3] - coord[0] * coord[2]));
        case 1:
            return new Vec3D(2.0 * (coord[1] * coord[2] - coord[0] * coord[3]),
                    coord[0] * coord[0] - coord[1] * coord[1] + coord[2]
                            * coord[2] - coord[3] * coord[3], 2.0 * (coord[2]
                                    * coord[3] + coord[0] * coord[1]));
        case 2:
            return new Vec3D(2.0 * (coord[1] * coord[3] + coord[0] * coord[2]),
                    2.0 * (coord[2] * coord[3] - coord[0] * coord[1]), coord[0]
                            * coord[0] - coord[1] * coord[1] - coord[2]
                                    * coord[2] + coord[3] * coord[3]);
        default:
            return null;
        }
    }

    /**
     * Construct versor from an orthonormal (rotation) matrix.
     *
     * @param qx image of X axis
     * @param qy image of Y axis
     * @param qz image of Z axis
     */
    private Versor(Vec3D qx, Vec3D qy, Vec3D qz) {
        super(fromRotation(qx, qy, qz));
    }

    /**
     * Construct versor rotating one pair of unit(!) vectors to another.
     *
     * @param fromA source vector A
     * @param fromB source vector B
     * @param toA target vector A
     * @param toB target vector B
     * @param a weight of vector A (weight of vector B is 1-a, respectively)
     * @return rotation with least weighted square error
     */
    public static Versor matchingRotation(Vec3D fromA, Vec3D fromB, Vec3D toA,
            Vec3D toB, double a) {
        double b = 1.0 - a;
        Vec3D from = new Vec3D(fromA).scale(a).add(new Vec3D(fromB).scale(b))
                .unit();
        Vec3D fromN = fromA.cross(fromB).unit();
        Vec3D fromC = from.cross(fromN);
        Vec3D to = new Vec3D(toA).scale(a).add(new Vec3D(toB).scale(b)).unit();
        Vec3D toN = toA.cross(toB).unit();
        Vec3D toC = to.cross(toN);
        return new Versor(from, fromN, fromC).inv().mul(
                new Versor(to, toN, toC));
    }

    /**
     * Extract rotation vector.
     *
     * @return vector pointing in parallel to the axis of rotation with a
     *         length proportional to the angle.
     */
    public Vec3D rot() {
        try {
            double sinHalfTetha = i().abs();
            return i()
                    .scale(2.0 * Math.atan2(sinHalfTetha, r()) / sinHalfTetha);
        } catch (ArithmeticException e) {
            return Vec3D.zero();
        }
    }

    /**
     * Rotation around longitudal (Z) axis.
     *
     * @return roll angle in degrees
     */
    public double roll() {
        return Math.toDegrees(Math.atan2(
                2.0 * (coord[0] * coord[3] + coord[1] * coord[2]),
                1.0 - 2.0 * (coord[3] * coord[3] + coord[1] * coord[1])));
    }

    /**
     * Rotation around lateral (X) axis.
     *
     * @return pitch angle in degrees
     */
    public double pitch() {
        return Math.toDegrees(Math.asin(2.0 * (coord[0] * coord[1] - coord[2] * coord[3])));
    }

    /**
     * Rotation around vertical (Y) axis.
     *
     * @return yaw angle in degrees
     */
    public double yaw() {
        return Math.toDegrees(Math.atan2(
                2.0 * (coord[0] * coord[2] + coord[3] * coord[1]),
                1.0 - 2.0 * (coord[1] * coord[1] + coord[2] * coord[2])));
    }
}
