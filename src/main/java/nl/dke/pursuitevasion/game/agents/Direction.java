package nl.dke.pursuitevasion.game.agents;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by nik on 26/02/17.
 */
public enum Direction
{
    NORTH,
    SOUTH,
    EAST,
    WEST,
    NORTH_EAST,
    NORTH_WEST,
    SOUTH_EAST,
    SOUTH_WEST;

    static Random random = new Random();

    public static int getAngle(Direction direction)
    {
        switch(direction)
        {
            case EAST:
                return 0;
            case NORTH_EAST:
                return 45;
            case NORTH:
                return 90;
            case NORTH_WEST:
                return 135;
            case WEST:
                return 180;
            case SOUTH_WEST:
                return 225;
            case SOUTH:
                return 270;
            case SOUTH_EAST:
                return 315;
            default:
                throw new IllegalArgumentException("You somehow gave a non-existing direction");
        }
    }

    public static Direction getRandomDirection(){
        Direction[] dir = Direction.values();
        return dir[random.nextInt(8)];
    }

    public static Direction getDirection(boolean north, boolean south, boolean east, boolean west)
    {
        if(north)
        {
            if(east)
            {
                return Direction.NORTH_EAST;
            }
            else if(west)
            {
                return Direction.NORTH_WEST;
            }
            else
            {
                return Direction.NORTH;
            }
        }
        else if(south)
        {
            if(east)
            {
                return Direction.SOUTH_EAST;
            }
            else if(west)
            {
                return Direction.SOUTH_WEST;
            }
            else
            {
                return Direction.SOUTH;
            }
        }
        else if(west)
        {
            return Direction.WEST;
        }
        else if(east)
        {
            return Direction.EAST;
        }
        else
        {
            return null;
        }
    }
    
}
