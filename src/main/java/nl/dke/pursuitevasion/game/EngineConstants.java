package nl.dke.pursuitevasion.game;

import java.awt.*;

/**
 * Created by nik on 03/03/17.
 *
 * color adder by Markus on 12/05/17.
 */
public class EngineConstants
{
    //todo fix this shit to make it realistic
    public final static double WALKING_SPEED =  40; // pixels
    public final static double TURNING_SPEED = 360; // degrees (1 circle rotation per second)
    public final static double VISION_RANGE  = 100; // pixels
    public final static double VISION_ANGLE  = 120; // degrees

    //if this is true, the game loop will never end!
    public final static boolean ALWAYS_LOOP = false;


    public final static Color FLOOR_COLOR = new Color(153,204,255);
    public final static Color FLOOR_ALT_COLOR = Color.ORANGE;
    public final static Color OBSTACLE_COLOR = new Color((float)0.8,(float)0.8,(float)0.8);//new Color(153,204,255);
    public final static Color OBSTACLE_ALT_COLOR = Color.GREEN;
    public final static Color EXIT_COLOR = new Color(255, 80, 80);
    public final static Color ENTRY_EVADER_COLOR =  new Color(102, 255, 51);
    public final static Color ENTRY_PURSUER_COLOR = new Color(102, 102, 255);
    private final static Color VISION_ARC_BASE_COLOR = Color.red;
    public final static Color VISION_ARC_COLOR = new Color(VISION_ARC_BASE_COLOR.getRed(), VISION_ARC_BASE_COLOR.getGreen(), VISION_ARC_BASE_COLOR.getBlue(), 20);
    public final static Color EVADER_COLOR = new Color(181, 19, 19);
    public final static Color PURSUER_COLOR = new Color(31, 94, 196);


}
