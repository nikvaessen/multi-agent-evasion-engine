package nl.dke.pursuitevasion.game.agents;

/**
 * Represent an angle in degrees
 *
 * Created by nik on 03/03/17.
 */
public class Angle
{
    private double angle;

    public Angle()
    {
        angle = 0;
    }

    public Angle(int degrees)
    {
        angle = degrees;
    }

    public Angle(Direction direction)
    {
        rotateTo(direction);
    }

    public double getAngle()
    {
        return angle;
    }

    public void rotate(int degree)
    {
        angle = (angle + degree) % 360;
    }

    public void rotateTo(Direction direction)
    {
        switch(direction)
        {
            case EAST:
                angle = 0;
                break;
            case NORTH_EAST:
                angle = 45;
                break;
            case NORTH:
                angle = 90;
                break;
            case NORTH_WEST:
                angle = 135;
                break;
            case WEST:
                angle = 180;
                break;
            case SOUTH_WEST:
                angle = 225;
                break;
            case SOUTH:
                angle = 270;
                break;
            case SOUTH_EAST:
                angle = 315;
                break;
        }
    }

}
