package org.epoint.avionics.math;

/**
 * A 2D vector with x, y coordinates.
 */
public class Vec2D extends Vec {

    /**
     * Create 2D vector from coordinates.
     *
     * @param x coordinate
     * @param y coordinate
     */
    public Vec2D(final double x, final double y) {
        super(new double[] {x, y});
    }

    /**
     * Construct 2D vector from radial coordinates.
     *
     * @param r radius
     * @param angle angle in degrees
     * @return vector with Cartesian coordinates in the same unit as r
     */
    public static Vec2D radial(double r, double angle) {
        double a = Math.toRadians(angle);
        return new Vec2D(r * Math.cos(a), r * Math.sin(a));
    }

    /**
     * Copy constructor.
     *
     * @param o other vector
     */
    public Vec2D(final Vec2D o) {
        super(o);
    }

    /**
     * Zero vector.
     */
    public Vec2D() {
        super(2);
    }

    /**
     * Unit vector.
     *
     * @param c axis index
     */
    public Vec2D(final int c) {
        super(2, c);
    }

    /**
     * Zero vector. Syntactic sugar.
     *
     * @return zero vector
     */
    public static Vec2D zero() {
        return new Vec2D();
    }

    /**
     * Unit vector. Syntactic sugar.
     *
     * @param a axis index
     * @return Unit vector along axis <code>a</code>
     */
    public static Vec2D axis(final int a) {
        return new Vec2D(a);
    }

    public Vec2D add(Vec2D o) {
        super.add(o);
        return this;
    }

    public Vec2D sub(Vec2D o) {
        super.sub(o);
        return this;
    }

    @Override
    public Vec2D scale(double s) {
        super.scale(s);
        return this;
    }

    @Override
    public Vec2D scale(double[] s) {
        super.scale(s);
        return this;
    }

    @Override
    public Vec2D inv() {
        super.inv();
        return this;
    }

    public double x() {
        return coord[0];
    }

    public double y() {
        return coord[1];
    }

    /**
     * Cross product. Does not alter this vector.
     *
     * @param o other vector
     * @return cosine of angle times the product of magnitudes
     */
    public double cross(final Vec2D o) {
        return x() * o.y() - y() * o.x();
    }

    /**
     * Complex multiplication. Does not alter this vector.
     *
     * @param o other vector
     * @return complex product
     */
    public Vec2D mul(Vec2D o) { return new Vec2D(x() * o.x() - y() * o.y(), x() * o.y() + y() * o.x()); }

    /**
     * @see Vec#unit()
     */
    @Override
    public Vec2D unit() {
        super.unit();
        return this;
    }
}
