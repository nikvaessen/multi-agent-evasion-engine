package nl.dke.pursuitevasion.game;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * This class represent a 2d vector
 * <p>
 * Created by nik on 07/05/17.
 */
public class Vector2D implements Serializable
{
    /**
     * The x component of the Vector2D
     */
    private double x;

    /**
     * The y component of the Vector2D
     */
    private double y;

    /**
     * Constructor that creates a Vector2D with length 0
     */
    public Vector2D()
    {
        x = 0;
        y = 0;
    }

    /**
     * Construct a Vector2D
     *
     * @param x the x component
     * @param y the y component
     */
    public Vector2D(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Construct a Vector2D which is identical to the given Vector2D
     *
     * @param v the Vector2D to copy
     */
    public Vector2D(Vector2D v)
    {
        this.x = v.x;
        this.y = v.y;
    }

    /**
     * Get a new Vector2D with the given x and y value added to this Vector2D
     *
     * @param x the x to add to this Vector2D
     * @param y the y to add to this Vector2D
     * @return the new Vector2D with x and y added to this Vector2D
     */
    public Vector2D add(double x, double y)
    {
        return new Vector2D(this.x + x, this.y + y);
    }

    /**
     * Get a new Vector2D with the given Vector2D added to this Vector2D
     *
     * @param v the Vector2D to add to this Vector2D
     * @return the new Vector2D with the given Vector2D added to this Vector2D
     */
    public Vector2D add(Vector2D v)
    {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    /**
     * Get a new Vector2D with the given x and y value subtracted from this Vector2D
     *
     * @param x the x to subtract from this Vector2D
     * @param y the y to subtract from this Vector2D
     * @return the new Vector2D with x and y subtracted from this Vector2D
     */
    public Vector2D subtract(double x, double y)
    {
        return add(-x, -y);
    }

    /**
     * Get a new Vector2D with the given Vector2D subtracted from this Vector2D
     *
     * @param v the Vector2D to subtract to this Vector2D
     * @return the new Vector2D with x and y added to this Vector2D
     */
    public Vector2D subtract(Vector2D v)
    {
        return add(-v.x, -v.y);
    }

    /**
     * Get a new Vector2D which is this Vector2D scaled by a given scalar value
     *
     * @param c the scalar value to scale this Vector2D with
     * @return the new scaled Vector2D
     */
    public Vector2D scale(double c)
    {
        return new Vector2D(c * this.x, c * this.y);
    }

    /**
     * Get the normalized Vector2D of this Vector2D
     *
     * @return the normalized Vector2D
     */
    public Vector2D norm()
    {
        double l = length();
        return new Vector2D(this.x / l, this.y / l);
    }

    /**
     * Get the length of the Vector2D
     *
     * @return the length
     */
    public double length()
    {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    /**
     * Calculate the distance from this vector to the given Vector2D
     *
     * @return the distance between this Vector2D and the given Vector2D
     */
    public double distance(Vector2D v)
    {
        return Math.sqrt(Math.pow(this.x - v.x, 2) + Math.pow(this.y - v.y, 2));
    }

    /**
     * Get the x component of this Vector2D
     *
     * @return the x component of this Vector2D
     */
    public double getX()
    {
        return x;
    }

    /**
     * Get the y component of this Vector2D
     *
     * @return the y component of this Vector2D
     */
    public double getY()
    {
        return y;
    }

    /**
     * Get the x component of this Vector2D
     *
     * @return the x component of this Vector2D
     */
    public void setX(double x) {
        this.x = x;
    }
    /**
     * Get the y component of this Vector2D
     *
     * @return the y component of this Vector2D
     */
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Vector2D)
        {
            Vector2D v = (Vector2D) o;
            return Math.abs(this.x - v.x) < 0.00001 &
                   Math.abs(this.y - v.y) < 0.00001;
        }
        return false;
    }

    public Point2D toPoint(){
        return new Point2D.Double(x, y);
    }

    // Creates a vector2D instance from an array of doubles
    // Presumes the array to be in [x,y] format.
    // Rejects arrays that are smaller than 2 elements.
    // Ignores elements beyond index 1.
    public static Vector2D fromDoubleArray(double[] coords){
        // check that the size is correct
        if(coords.length < 2)
        {
            throw new IllegalArgumentException("coordinate array should have at least 2 elements");
        }
        return new Vector2D(coords[0], coords[1]);
    }

    public static Vector2D fromPoint2D(Point2D point){
        return new Vector2D(point.getX(), point.getY());
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(x, y);
    }

    @Override
    public String toString()
    {
        return String.format("Vector2D[x:%f,y:%f]", this.x, this.y);
    }

    public Vector2D copy() {
        return new Vector2D(this.x,this.y);
    }
}
