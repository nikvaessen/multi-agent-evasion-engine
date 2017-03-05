package nl.dke.pursuitevasion.game;

import nl.dke.pursuitevasion.game.agents.AbstractAgent;
import nl.dke.pursuitevasion.game.agents.AgentCommand;
import nl.dke.pursuitevasion.game.agents.AgentRequest;
import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
;

/**
 * Created by nik on 2/8/17.
 */
public class Engine
{

    private final static Logger logger = LoggerFactory.getLogger(Engine.class);

    private final int desiredFPS;

    private long desiredIterationLength; //in ms

    private Collection<AbstractAgent> agents;

    private MapViewPanel mapViewPanel;

    private Thread gameLoopThread;

    public Engine(Collection<AbstractAgent> agents, MapViewPanel viewPanel, int desiredFPS)
    {
        this.desiredFPS = desiredFPS;
        this.desiredIterationLength = Math.round(1000d / (double) desiredFPS);
        this.agents = agents;
        this.mapViewPanel = viewPanel;
    }

    public Engine(Collection<AbstractAgent> agents, int desiredFPS)
    {
        this(agents, null, desiredFPS);
    }

    public synchronized void start()
        throws IllegalStateException
    {
        if(gameLoopThread == null || !gameLoopThread.isAlive())
        {
            gameLoopThread = new Thread(new GameLoopRunnable());
            gameLoopThread.start();
        }
        else
        {
            throw new IllegalStateException("Starting engine while it's already running");
        }
    }

    private class GameLoopRunnable
        implements Runnable
    {
        private LinkedList<AgentRequest> requests = new LinkedList<>();

        private LinkedList<AgentCommand> commands = new LinkedList<>();

        private double metersPerIteration = EngineConstants.WALKING_SPEED / desiredIterationLength;

        private double rotationPerIteration = EngineConstants.TURNING_SPEED / desiredIterationLength;

        @Override
        public void run()
        {
            loop();
        }

        /**
         * Continuous-time loop:
         * 1. Check for game over, or if any agent has been spotted
         * 3. Request agents to change their direction if they so desire
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

                // 2. Check agents
                for(AbstractAgent agent : agents)
                {
                    if(agent.completedRequest() && agent.hasRequest())
                    {
                        requests.add(agent.getRequest());
                    }
                }

                // 3. Validate legality of moves
                for(AgentRequest request : requests)
                {
                    handleRequest(request);
                }

                // 4. Make the moves


                // 5. Update the view
                if(mapViewPanel != null)
                {
                    mapViewPanel.repaint();
                    logger.trace("Updated MapViewPanel");
                }

                // 6. wait
                msPassed = System.currentTimeMillis() - iterationStartTime;
                if(msPassed < desiredIterationLength)
                {
                    try
                    {
                        logger.trace("waiting for {} ms", desiredIterationLength - msPassed);
                        Thread.sleep(desiredIterationLength - msPassed);
                    } catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    logger.warn("GameLoop took {} ms while only allowed to take {} ms. Exceeded: {} ms",
                            msPassed, desiredIterationLength, msPassed - desiredIterationLength);
                }
            }

            logger.info("Game successfully terminated in {} ms", System.currentTimeMillis() - startTime);
        }

        private void handleRequest(AgentRequest request)
        {
            AbstractAgentTask task = request.peek();
            commands.add(task.handle(request.getAgent(), metersPerIteration, rotationPerIteration));
        }

    }


}
