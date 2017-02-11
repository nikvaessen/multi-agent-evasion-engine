package nl.dke.pursuitevasion.gui;

import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import java.awt.*;


/**
 * Created by Nibbla on 11.02.2017.
 *
 * This classed is used to draw the actuall model on the screen.
 */
public class ModelView extends JPanel {

    private Map map;


    public ModelView(int prefaredWidht, int prefaredHeight) {
        this.setPreferredSize(new Dimension(prefaredWidht,prefaredHeight));

    }

    public void setMap(Map m){
        this.map  = m;

        repaint();
    }


    @Override
    protected void paintComponent(Graphics g1d) {
        super.paintComponent(g1d);

        Graphics2D g2d = (Graphics2D) g1d;

    }
}
