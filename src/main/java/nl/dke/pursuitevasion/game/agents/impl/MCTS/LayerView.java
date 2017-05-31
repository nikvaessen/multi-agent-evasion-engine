package nl.dke.pursuitevasion.game.agents.impl.MCTS;

import nl.dke.pursuitevasion.game.Vector2D;
import nl.dke.pursuitevasion.gui.simulator.MapViewPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

/**
 * Created by nibbla on 16.01.17.
 */
public class LayerView extends JPanel{
    private final Dimension preservedSize;
    private final MCTS_2 mcts_2;

    ArrayList<ArrayList<NodeTree_2>> values;
    private LinkedList<Polygon> polygones  = new LinkedList<>();;
    private HashMap<Polygon, NodeTree_2> messages = new HashMap<>(4000);
    private NodeTree_2 root;

    public LayerView(NodeTree_2 r, MCTS_2 mcts_2, Dimension preveredSize, MCTS_2.MCTSViewSettings mctsViewSettings){
        super(new BorderLayout());

        this.mcts_2 = mcts_2;
        setPreferredSize(preveredSize);
        setBackground(Color.white);

        this.preservedSize = preveredSize;


        root = r;




        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

                    for (Polygon p : polygones) {
                        if (p.contains(e.getPoint())) {
                            NodeTree_2 clickedUpon =  messages.get(p);
                            if (e.getButton()== MouseEvent.BUTTON1) {
                                JOptionPane.showMessageDialog(null, clickedUpon.toString(), "Info", JOptionPane.OK_CANCEL_OPTION);
                            }
                            if (e.getButton()== MouseEvent.BUTTON2) {
                                root = clickedUpon;
                                repaint();
                            }


                        }
                    }
                if (e.getButton()== MouseEvent.BUTTON3) {
                    if (root.getParent() != null) {
                        root = root.getParent();
                        repaint();
                    }

                }


            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private void createTree(NodeTree_2 newRoot) {
        NodeTree_2 root = newRoot;
        if (root  == null) return;
        int height = root.getHeight();
        System.out.println("Height: " + height);
        int difference =root.getDepth();

        values = new ArrayList<>();
        for (int i = 0; i <= height; i++) {
            values.add(new ArrayList<>());
        }
        ArrayList<NodeTree_2> breadthFirst = root.breadthFirst();
        int bfs = breadthFirst.size();
        for (int i = 0; i <bfs; i++) {
            NodeTree_2 node = breadthFirst.get(i);
            int depth = node.getDepth()-difference;
            values.get(depth).add(node);
        }
        for (int i = 0; i <values.size(); i++) {
            System.out.println("Level " + i + " has " + values.get(i).size() + "members");
        }
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.red);
        //g.fillRect(0,0,(int)preservedSize.getWidth(),(int)preservedSize.getHeight());

        messages.clear();
        messages = new HashMap<>(4000);
        polygones.clear();
        polygones = new LinkedList<>();
        createTree(root);
        double layerHeight = this.getHeight() / values.size() / 2;
        double layerWidth = this.getWidth();
        Random r = new Random();

        Map<NodeTree_2,int[]> map = new HashMap<>(200);
        for (int i = 0; i <values.size(); i++){
            ArrayList<Integer> leftiesNew = new ArrayList<>(200);
            int layerHeight1= (int) (layerHeight*(i*2));
            int layerHeight2= (int) (layerHeight*(i*2+1));




            double totalSimulationsInThisLayer = 0;
            ArrayList<NodeTree_2> singleLayer = values.get(i);
            for (int j = 0; j <singleLayer.size(); j++){
                totalSimulationsInThisLayer+= singleLayer.get(j).getGames();
            }

            int lastLayerLeft = 0;

            int lastLayerRight = 0;


            int others = 0;


            for (int j = 0; j <singleLayer.size(); j++){
                double choiseTotalGames = singleLayer.get(j).getGames();
                double ratio = choiseTotalGames/totalSimulationsInThisLayer;
                int choiceWidth = (int) (ratio* layerWidth);
                if (choiceWidth<1) {others++; continue;}

                lastLayerRight = lastLayerLeft + choiceWidth;
                g.setColor(new Color(r.nextInt()));
                g.fillRect(lastLayerLeft,layerHeight1,choiceWidth,layerHeight2-layerHeight1);
                int[] valuePair = {lastLayerLeft,lastLayerRight};
                map.put(singleLayer.get(j),valuePair);

                int[] XpointsSqare = new int[4];
                int[] YpointsSqare = new int[4];
                XpointsSqare[0] = lastLayerLeft;
                XpointsSqare[1] = lastLayerRight;
                XpointsSqare[2] = lastLayerRight;
                XpointsSqare[3] = lastLayerLeft;

                YpointsSqare[2] = layerHeight2;
                YpointsSqare[3] = layerHeight2;
                YpointsSqare[0] = layerHeight1;
                YpointsSqare[1] = layerHeight1;
                Polygon p = new Polygon(XpointsSqare,YpointsSqare,4);
                polygones.add(p);
                messages.put(p,singleLayer.get(j));


                if(i!=0 ) {
                    int layerHeight_minus1= (int) (layerHeight*(i*2-1));

                    valuePair = map.get(singleLayer.get(j).getParent());
                    if (valuePair==null)break;
                    //draw from currrent lastlayerleftAndRight to parent left and right a polygon!!!
                    int[] Xpoints = new int[4];
                    int[] Ypoints = new int[4];
                    Xpoints[0] = (valuePair[1]+valuePair[0])/2;
                    Xpoints[1] = (valuePair[1]+valuePair[0])/2;
                    Xpoints[2] = lastLayerRight;
                    Xpoints[3] = lastLayerLeft;

                    Ypoints[2] = layerHeight1;
                    Ypoints[3] = layerHeight1;
                    Ypoints[0] = layerHeight_minus1;
                    Ypoints[1] = layerHeight_minus1;

                    g.fillPolygon(Xpoints,Ypoints,4);
                }




                lastLayerLeft = lastLayerRight;
            }

            //print others wich are too small
            double ratio = others/totalSimulationsInThisLayer;
            int choiceWidth = (int) (ratio* layerWidth);
            lastLayerRight = lastLayerLeft + choiceWidth;
            g.setColor(new Color(r.nextInt()));
            g.fillRect(lastLayerLeft,layerHeight1,choiceWidth,layerHeight2-layerHeight1);

            lastLayerLeft = lastLayerRight;


            //go from old ones from left to right and










        }


    }

    public void paintOverView(Graphics g, MapViewPanel mapViewPanel) {
        createTree(root);
        Map<NodeTree_2,double[]> map = new HashMap<>(200);
        double layerWidth = this.getWidth();

        for (int i = 0; i <values.size(); i++) {
            ArrayList<Integer> leftiesNew = new ArrayList<>(200);
           // int layerHeight1 = (int) (layerHeight * (i * 2));
           // int layerHeight2 = (int) (layerHeight * (i * 2 + 1));

            int lastLayerLeft = 0;
            int lastLayerRight = 0;
            int others = 0;
            double totalSimulationsInThisLayer = 0;
            ArrayList<NodeTree_2> singleLayer = values.get(i);
            for (int j = 0; j < singleLayer.size(); j++) {
                totalSimulationsInThisLayer += singleLayer.get(j).getMaximalPossiblePoints();
            }

            for (int j = 0; j <singleLayer.size(); j++) {
                double choiseTotalGames = singleLayer.get(j).getMaximalPossiblePoints();
                double ratio = choiseTotalGames / totalSimulationsInThisLayer;
                int choiceWidth = (int) (ratio* layerWidth);
               // if (choiceWidth<1) {others++; continue;}
                lastLayerRight = lastLayerLeft + choiceWidth;
                layerWidth = 2*Math.PI * values.get(i).get(j).getMetricalDistanceIfPlayerToRoot(null);
               // g.setColor(new Color(r.nextInt()));
               // g.fillRect(lastLayerLeft,layerHeight1,choiceWidth,layerHeight2-layerHeight1);
                double[] valuePair = {lastLayerLeft,lastLayerRight,layerWidth,ratio};
                NodeTree_2 n = singleLayer.get(j);
                map.put(n,valuePair);
                drawNode(g,n,valuePair);
                if(i!=0 ) {
                    //int layerHeight_minus1 = (int) (layerHeight * (i * 2 - 1));

                    valuePair = map.get(singleLayer.get(j).getParent());

                    NodeTree_2 np = singleLayer.get(j).getParent();


                    drawLine(g,n,np,valuePair,new Color(0f,1f,0f,0.5f));
                }
            }
            NodeTree_2 n = mcts_2.getBestMove();
            double[] valuePair = {lastLayerLeft,lastLayerRight,layerWidth,n.getWins()};
            drawLine(g,n,n,valuePair,Color.BLUE);
        }

    }

    private void drawLine(Graphics g, NodeTree_2 n, NodeTree_2 np, double[] valuePair, Color c) {
        Graphics2D g2 = (Graphics2D)g;
        Stroke stroke = g2.getStroke();
        g2.setStroke(new BasicStroke(4));
        try{
            Vector2D vector1 = n.getMove().getStartLocation();
            //NodeTree_2 n2 = n.getParent().getParent().getParent().getParent();
            Vector2D vector2  = n.getMove().getEndLocation();
            double num = n.getMaximalPossiblePoints();
            if (num == 0) return;
            double winRatio = n.getWins()/num;

            //Color c = new Color( (float)(1-(1*winRatio)),(float)winRatio,0f,1f);

            g.setColor(c);
            g2.drawLine((int)vector1.getX(),(int)vector1.getY(),(int)vector2.getX(),(int)vector2.getY());
           // System.out.println("Draw node with between: " + vector1 +"  " + vector2);
        }catch (NullPointerException o){
            System.out.println("no line draw");
        }

        g2.setStroke(stroke);

    }

    private void drawNode(Graphics g, NodeTree_2 n, double[] valuePair) {

        Vector2D vector2D = n.getMove().getEndLocation();
       // System.out.println("Draw node at location: " + vector2D);
        double area = valuePair[3] * 10;
        double radius = Math.sqrt(area/Math.PI);
        double x = vector2D.getX();// - radius;
        double y = vector2D.getY();// - radius;
        double num = n.getMaximalPossiblePoints();
        //if (num == 0) return;

        double winRatio ;
        if (num != 0) {
            winRatio =  n.getWins()/num;
        //    System.out.println("Draw node with winRatio: " + winRatio +" " + n.getWins()+" " + num + " " +area);
        }else{
            radius = 10;
        }


      //  Color c = new Color( (float)(1-(1*winRatio)),(float)winRatio,0f,1f);
        Color c = Color.GREEN;
        g.setColor(c);
        g.drawOval((int)x,(int)y,(int)radius*2,(int)radius*2);
       // System.out.println("Draw node with radius: " + x+" " + y+" " + radius);
    }
}
