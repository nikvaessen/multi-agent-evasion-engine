package nl.dke.pursuitevasion.game;

import com.sun.javafx.geom.Line2D;
import nl.dke.pursuitevasion.game.agents.*;
import nl.dke.pursuitevasion.game.agents.tasks.AbstractAgentTask;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;
import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;
import nl.dke.pursuitevasion.map.impl.Obstacle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Predicate;

;

/**
 * Created by nik on 2/8/17.
 */
public class Engine
{

    private final static Logger logger = LoggerFactory.getLogger(Engine.class);

    private final int desiredFPS;

    private long desiredIterationLength; //in ms

    private Map map;

    private Collection<AbstractAgent> agents;

    private MapViewPanel mapViewPanel;

    private Thread gameLoopThread;

    public Engine(Map map, Collection<AbstractAgent> agents, MapViewPanel viewPanel, int desiredFPS)
    {
        this.map = map;
        this.desiredFPS = desiredFPS;
        this.desiredIterationLength = Math.round(1000d / (double) desiredFPS);
        this.agents = agents;
        this.mapViewPanel = viewPanel;

        //set all agents to the correct vision angle and degrees
        agents.forEach(agent ->
                       {
                           agent.setVisionRange(EngineConstants.VISION_RANGE);
                           agent.setVisionAngle(EngineConstants.VISION_ANGLE);
                       });
    }

    public Engine(Map map, Collection<AbstractAgent> agents, int desiredFPS)
    {
        this(map, agents, null, desiredFPS);
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

        private LinkedList<AbstractAgent> evaders = new LinkedList<>();

        private LinkedList<AbstractAgent> pursuers = new LinkedList<>();

        private double metersPerIteration = EngineConstants.WALKING_SPEED / desiredIterationLength;

        private double rotationPerIteration = EngineConstants.TURNING_SPEED / desiredIterationLength;

        @Override
        public void run()
        {
            // put all the agents in the correct list
            for(AbstractAgent agent : agents)
            {
                if(agent.isEvader())
                {
                    evaders.push(agent);
                }
                else
                {
                    pursuers.push(agent);
                }
            }

            // start the game loop
            loop();
        }

        /**
         * Continuous-time loop:
         * 1. Check for game over, or if any agent has been spotted
         * 2. Request agents to change their direction if they so desire
         * 3. Validate if current behaviour of agents is legal
         * 4. Update the environment based on current behaviour
         * 5. Agents which are allowed to have hidden information are given an update
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
                if(logger.isDebugEnabled())
                {
                    logger.debug("Starting game loop iteration {} at {} ms", count, iterationStartTime);
                }

                // 1. Check if game is over
                removeCaughtEvaders();
                if(evaders.isEmpty() && !EngineConstants.ALWAYS_LOOP)
                {
                    break;
                }

                // Determine which agents are in the viewing range of other agents
                for(AbstractAgent agent : agents){
                    // make agents update their vision arcs
                    agent.getVisionArc().update(agents);


                    // make information available to agents
                }

                // 2. Check agents
                for(AbstractAgent agent : agents)
                {
                    if(agent.hasRequest())
                    {
                        AgentRequest request = agent.getRequest();
                        requests.add(request);

                        if(logger.isTraceEnabled())
                        {
                            logger.trace("Added new Request to list. Request: {} Size: {}", request, requests.size());
                        }
                    }
                }

                // 3. Validate legality of moves

                // first filter out any invalidated/completed requests
                if(logger.isDebugEnabled())
                {
                    logger.debug("Handling {} requests", requests.size());
                }
                requests.removeIf(new Predicate<AgentRequest>()
                {
                    @Override
                    public boolean test(AgentRequest agentRequest)
                    {
                        return agentRequest.isCompleted();
                    }
                });

                // go over all requests and get the agent commands
                if(logger.isDebugEnabled())
                {
                    logger.debug("{} requests left after deleting all completed requests", requests.size());
                }

                for(AgentRequest request : requests)
                {
                    logger.trace("Handling request {}, which is completed: {}", request, request.isCompleted());
                    handleRequest(request);
                }

                // check if all commands are valid
                validateCommands();

                // 4. Make the moves
                if(logger.isDebugEnabled())
                {
                    logger.debug("Applying {} commands!", commands.size());
                    if(logger.isTraceEnabled())
                    {
                        for(AgentCommand command : commands)
                        {
                            logger.trace("command: " + command);
                        }
                    }
                }
                commands.forEach(AgentCommand::apply);
                commands.clear();

                // 5. Update the view
                if(mapViewPanel != null)
                {
                    mapViewPanel.repaint();
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Updated MapViewPanel");
                    }
                }

                // 6. wait
                msPassed = System.currentTimeMillis() - iterationStartTime;
                if(msPassed < desiredIterationLength)
                {
                    try
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("waiting for {} ms", desiredIterationLength - msPassed);
                        }
                        Thread.sleep(desiredIterationLength - msPassed);
                    }
                    catch(InterruptedException e)
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
            double allowedMeters = metersPerIteration;
            double allowedRotation = rotationPerIteration;
            AbstractAgentTask task = null;

            if(logger.isTraceEnabled())
            {
                logger.trace("Resolving request {}", request);
                logger.trace("Request for Agent {}", request.getAgent());
                logger.trace("Allowed meters: {} Allowed rotation: {}", allowedMeters, allowedRotation);
            }

            do
            {
                try
                {
                    task = request.peek();
                    AgentCommand command = task.handle(request.getAgent(), allowedMeters, allowedRotation);
                    commands.add(command);
                    allowedMeters -= command.getMovedDistance();
                    allowedRotation -= command.getRotatedDistance();

                    if(logger.isTraceEnabled())
                    {
                        logger.trace("Added to commands: {} ", command);
                        logger.trace("peeked request: {}", task);
                        logger.trace("allowed meters left: {}", allowedMeters);
                        logger.trace("allowed rotation left: {}", allowedRotation);
                        logger.trace("request is completed: {}", request.isCompleted());
                    }
                }
                catch(IllegalStateException e)
                {
                    logger.error("Cannot resolve new task", e);
                    break;
                }
            }
            while(!request.isCompleted() && Math.abs(allowedMeters - 0) > 0.001
                  && Math.abs(allowedRotation - 0) > 0.001);

            if(logger.isTraceEnabled())
            {
                logger.trace("Broke out of while loop. The request {} has been handled", request);
            }
        }

        private void validateCommands()
        {
            commands.forEach(this::outOfBoundCorrection);
        }

        //todo fix
        private void outOfBoundCorrection(AgentCommand command)
        {
            if(command.isLocationChanged())
            {
                Vector2D location = command.getNewLocation();
                Floor floor = command.getAgent().getFloor();
                int radius = command.getAgent().getRadius();

                Ellipse2D.Double circle = new Ellipse2D.Double(
                        location.getX() - radius,
                        location.getY() - radius,
                        radius * 2,
                        radius * 2);

                if(!containing(floor.getPolygon(), circle, false))
                {
                    int ind = commands.indexOf(command);
                    commands.remove(ind);
                }

                for(Obstacle obs : floor.getObstacles())
                {
                    if(containing(obs.getPolygon(), circle, true))
                    {
                        int ind = commands.indexOf(command);
                        commands.remove(ind);
                    }
                }

            }
        }

        private boolean containing(Polygon bb, Ellipse2D circle, boolean obstacle)
        {
            Point north = new Point((int) (circle.getX() + (circle.getHeight() / 2)), (int) (circle.getY()));
            Point south = new Point((int) (circle.getX() + (circle.getHeight() / 2)),
                                    (int) (circle.getY() + circle.getHeight()));
            Point east = new Point((int) (circle.getX() + (circle.getHeight())),
                                   (int) (circle.getY() + circle.getHeight() / 2));
            Point west = new Point((int) (circle.getX()), (int) (circle.getY() + circle.getHeight() / 2));
            Point southeast = new Point(
                (int) ((circle.getX() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.cos(
                    0.25 * Math.PI))),
                (int) ((circle.getY() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.sin(
                    0.25 * Math.PI))));
            Point southwest = new Point(
                (int) ((circle.getX() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.cos(
                    0.75 * Math.PI))),
                (int) ((circle.getY() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.sin(
                    0.75 * Math.PI))));
            Point northwest = new Point(
                (int) ((circle.getX() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.cos(
                    1.25 * Math.PI))),
                (int) ((circle.getY() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.sin(
                    1.25 * Math.PI))));
            Point northeast = new Point(
                (int) ((circle.getX() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.cos(
                    1.75 * Math.PI))),
                (int) ((circle.getY() + (circle.getHeight() / 2)) + ((circle.getHeight() / 2) * Math.sin(
                    1.75 * Math.PI))));
            if(obstacle)
            {
                if(bb.contains(north) || bb.contains(south) || bb.contains(east) || bb.contains(west) || bb.contains(
                    southeast) || bb.contains(southwest) || bb.contains(northeast) || bb.contains(northwest))
                {
                    return true;
                }
            }
            else if(bb.contains(north) && bb.contains(south) && bb.contains(east) && bb.contains(west) && bb.contains(
                southeast) && bb.contains(southwest) && bb.contains(northeast) && bb.contains(northwest))
            {
                return true;
            }
            return false;
        }

        private void removeCaughtEvaders()
        {
            //loop over all evaders
            for(AbstractAgent evader : evaders)
            {
                //check if any pursuer close enough to them
                for(AbstractAgent pursuer : pursuers)
                {
                    if(agentsOverlap(evader, pursuer))
                    {
                        agents.remove(evader);
                        evaders.remove(evader);
                    }
                }
            }
        }

        private boolean agentsOverlap(AbstractAgent agent1, AbstractAgent agent2)
        {
            Vector2D location1 = agent1.getLocation();
            Vector2D location2 = agent2.getLocation();
            double distance = location2.subtract(location1).length();

            int radius1 = agent1.getRadius();
            int radius2 = agent2.getRadius();

            if (radius2 >= radius1 && distance <= (radius2 - radius1)) //circle 1 is inside circle 2
            {
                return true;
            }
            else if (radius1 >= radius2 && distance <= (radius1 - radius2))  //circle 2 is inside circle 1
            {
                return true;
            }
            else //circles do not overlap if true
            {
                return !(distance > (radius1 + radius2));
            }
        }
    }

}