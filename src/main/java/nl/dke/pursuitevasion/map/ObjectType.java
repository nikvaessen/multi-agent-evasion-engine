package nl.dke.pursuitevasion.map;

/**
 * Every ObjectType which can be placed in a map
 *
 * The floor is the main object of a map
 *
 * A floor can contain a gate towards another floor
 *
 * A floor can contain an obstacle
 *
 * Created by nik on 2/8/17.
 */
public enum ObjectType
{
    FLOOR,
    GATE,
    OBSTACLE
}
