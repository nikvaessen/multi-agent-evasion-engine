package nl.dke.pursuitevasion.game;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
;

/**
 * Created by nik on 2/8/17.
 */
public class Engine
{

    private final static Logger logger = LoggerFactory.getLogger(Engine.class);

    private final int desiredFPS;

    private long minimalWaitTime;

    private Collection<AbstractAgent> agents;

    private MapViewPanel mapViewPanel;

    public Engine(Collection<AbstractAgent> agents, MapViewPanel viewPanel, int desiredFPS)
    {
        this.desiredFPS = desiredFPS;
        this.minimalWaitTime = Math.round(1000d / (double) desiredFPS);
        this.agents = agents;
        this.mapViewPanel = viewPanel;
    }

    public void start()
    {
        new Thread(this::loop).start();
    }

    /**
     * Continuous-time loop:
     * 1. Check for game over, or if any agent has been spotted
     * 3. Check input buffer for user input to potentially change user-controlled agent
     * 3. Validate if current behaviour of agents is legal
     * 4. Update the environment based on current behaviour
     * 5. NPC-agents which are allowed to have hidden information are given an update
     * 6. Render on screen
     * 7. Wait if there is time left.
     */
    private void loop()
    {
        // house keeping variables for loop
        long startTime = System.currentTimeMillis(), iterationStartTime, msPassed;
        int count = 0;
        while(true)
        {
            //update housekeeping variables and log
            iterationStartTime = System.currentTimeMillis();
            count++;
            logger.trace("Starting game loop iteration {} at {} ms", count, iterationStartTime);

            // 1. Check if game is over
            //todo implement game over checking
            if(System.currentTimeMillis() - startTime > 10000) //10 seconds
            {
                break;
            }

            // 2. Check on user-controlled agent


            // 3. Validate legality of moves

            // 4. Make the moves

            // 5. Update the view
            mapViewPanel.repaint();
            logger.trace("Updated MapViewPanel");

            // 6. wait
            msPassed = System.currentTimeMillis() - iterationStartTime;
            if(msPassed < minimalWaitTime)
            {
                try
                {
                    logger.trace("waiting for {} ms", minimalWaitTime - msPassed);
                    Thread.sleep(minimalWaitTime - msPassed);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                logger.warn("GameLoop took {} ms while only allowed to take {} ms. Exceeded: {} ms",
                        msPassed, minimalWaitTime, msPassed - minimalWaitTime);
            }
        }

        logger.info("Game successfully terminated in {} ms", System.currentTimeMillis() - startTime);
    }

}
