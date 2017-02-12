package nl.dke.pursuitevasion.gui;

import nl.dke.pursuitevasion.map.impl.Floor;
import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;


/**
 * Created by Nibbla on 11.02.2017.
 *
 * This classed is used to draw the actuall model on the screen.
 */
public class ModelView extends JPanel {

    private Map map;
    private AffineTransform affineTransform;
    private Point2D lastClickedPoint;


    public ModelView(int prefaredWidht, int prefaredHeight) {
        this.setPreferredSize(new Dimension(prefaredWidht,prefaredHeight));
        this.affineTransform = new AffineTransform();

        mouseListenerFunction();
    }

    private void mouseListenerFunction() {
        final ModelView mv = this;

        addMouseListener(new MouseListener() {


            @Override
            public void mouseClicked(MouseEvent e) {
                Point screenCenter = new Point(mv.getWidth() / 2, mv.getHeight() / 2);

                if(lastClickedPoint == null) lastClickedPoint = screenCenter;

                if (e.getButton() == MouseEvent.BUTTON3){
                        Point2D clickedPoint = new Point();

                        double translateX = clickedPoint.getX()-screenCenter.getX();
                        double translateY = clickedPoint.getY()-screenCenter.getY();

                        //affineTransform.deltaTransform(e.getPoint(),clickedPoint);
                        affineTransform.translate(translateX,translateY);
                        //lastClickedPoint = new Point((int)translateX,(int)translateY);

                    }

                if (e.getButton() == MouseEvent.BUTTON2){
                    affineTransform.setToIdentity();
                    lastClickedPoint = new Point(mv.getWidth()/2,mv.getHeight()/2);
                }

                repaint();
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

    public void setMap(Map m){
        this.map  = m;

        repaint();
    }


    @Override
    protected void paintComponent(Graphics g1d) {
        super.paintComponent(g1d);

        final ModelView mv = this;
        if(lastClickedPoint == null) lastClickedPoint = new Point(mv.getWidth()/2,mv.getHeight()/2);

        Graphics2D g2d = (Graphics2D) g1d;
        AffineTransform saveXform = g2d.getTransform();
        Stroke saveStroke = g2d.getStroke();

        g2d.fillRect(0,0,this.getWidth(),this.getHeight());

        g2d.transform(affineTransform);

        if (map == null){
            int x = this.getWidth();
            int y = this.getHeight();
            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(Color.cyan);
            g2d.drawLine(x*1/3,y*1/3,x*2/3,y*2/3);

            g2d.setColor(Color.RED);
            g2d.fillOval((int)lastClickedPoint.getX()-5,(int)lastClickedPoint.getY()-5,10,10);
        }

        g2d.setTransform(saveXform);
        g2d.setStroke(saveStroke);
    }

    protected void resetView(){
        if (affineTransform == null) return;
        affineTransform.setToIdentity();
    }

}
