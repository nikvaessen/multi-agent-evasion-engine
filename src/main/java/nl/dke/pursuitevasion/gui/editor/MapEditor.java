package nl.dke.pursuitevasion.gui.editor;

import javafx.scene.shape.*;
import nl.dke.pursuitevasion.gui.ModelView;
import nl.dke.pursuitevasion.gui.Voronoi;
import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import javax.swing.Box;
import java.awt.*;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.Console;
import java.util.*;
import java.util.List;

/**
 * Created by nik on 2/8/17.
 * Modified by Nibbla on 2/8/17.
 *
 * This will be the view of the editor
 */
public class MapEditor extends JPanel
{
    private final OptionView optionView;
    private final JTabbedPane EastView;
    private final ObjectSelectionView objectSelectionView;
    private final FileView fileView;
    private ModelView modelView;
    private Map map;


    public MapEditor(int prefaredWidht, int prefaredHeight) {
        this.setPreferredSize(new Dimension(prefaredWidht, prefaredHeight));
        this.setLayout(new BorderLayout(5, 5));

        this.modelView = new ModelView(prefaredWidht*4/5,prefaredHeight);

        this.add(modelView,BorderLayout.CENTER);




        this.EastView = new JTabbedPane();
        EastView.setPreferredSize(new Dimension(prefaredWidht*1/5,prefaredHeight));
        this.optionView = new OptionView(prefaredWidht*1/5,prefaredHeight);

        this.objectSelectionView = new ObjectSelectionView(prefaredWidht*1/5,prefaredHeight);
        this.fileView = new FileView(prefaredWidht*1/5,prefaredHeight);

        EastView.add(fileView,"FileView");
        EastView.add(optionView,"Level Generator");
        EastView.add(objectSelectionView,"Objects");


        this.add(EastView,BorderLayout.EAST);
    }

    /** Basic test main for the editor.
     * Creates the jframe of the editor and in there creates a simple level
     *
     * @param args
     */
    public static void main(String[] args){
        JFrame frame = new JFrame("testEditor");
        frame.setLayout(new BorderLayout(5,5));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        MapEditor mapEditor = new MapEditor(600,400);
        frame.add(mapEditor,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

    }

    private class OptionView extends JPanel {
        public OptionView(int prefaredWidth, int prefaredHeight) {
            Random r = new Random();
            this.setPreferredSize(new Dimension(prefaredWidth,prefaredHeight));
            this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
            OptionView optionView = this;


            JLabel l2 = new JLabel("Name");
            JTextField text2 = new JTextField("Map" + r.nextInt(100)); text2.setMaximumSize(new Dimension(prefaredWidth,25));
            optionView.add(l2); text2.setAlignmentX(Component.LEFT_ALIGNMENT);
            optionView.add(text2);

            JLabel l1 = new JLabel("Max Number Nodes");
            SpinnerModel sm1 = new SpinnerNumberModel(48, 0, 1024, 12); //default value,lower bound,upper bound,increment by
            JSpinner spinner1 = new JSpinner(sm1);
            ((JSpinner.DefaultEditor)spinner1.getEditor()).getTextField().setColumns(10);
            spinner1.setMaximumSize(new Dimension(prefaredWidth,25));
            optionView.add(l1); spinner1.setAlignmentX(Component.LEFT_ALIGNMENT);
            l1.setLabelFor(spinner1);
            optionView.add(spinner1);



            JLabel l3 = new JLabel("Width");
            SpinnerModel sm3 = new SpinnerNumberModel(0, 0, modelView.getWidth(), 1); //default value,lower bound,upper bound,increment by
            JSpinner spinner3 = new JSpinner(sm3);
            ((JSpinner.DefaultEditor)spinner3.getEditor()).getTextField().setColumns(10);
            spinner3.setMaximumSize(new Dimension(prefaredWidth,25));
            optionView.add(l3); spinner3.setAlignmentX(Component.LEFT_ALIGNMENT);
            l1.setLabelFor(spinner3);
            optionView.add(spinner3);

            JLabel l4 = new JLabel("Height");
            SpinnerModel sm4 = new SpinnerNumberModel(600, 0, 1024, 9); //default value,lower bound,upper bound,increment by
            JSpinner spinner4 = new JSpinner(sm4);
            spinner4.setMaximumSize(new Dimension(prefaredWidth,25));
            optionView.add(l4); spinner4.setAlignmentX(Component.LEFT_ALIGNMENT);
            optionView.add(spinner4);

            JLabel l5 = new JLabel("Word5");
            SpinnerModel sm5 = new SpinnerNumberModel(400, 0, 800, 9); //default value,lower bound,upper bound,increment by
            JSpinner spinner5 = new JSpinner(sm5); spinner5.setPreferredSize(new Dimension(200,50));
            spinner5.setMaximumSize(new Dimension(prefaredWidth,25));
            spinner5.setAlignmentX(Component.LEFT_ALIGNMENT);

            optionView.add(l5);
            optionView.add(spinner5);

          // optionView.add(new Swing.)

            JButton generate = new JButton("Generate Map");
            generate.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    generateVronoiWorld((int) sm1.getValue(),(int)sm4.getValue(),(int)sm5.getValue());

                }
            });
            optionView.add(generate,Component.LEFT_ALIGNMENT); optionView.setPreferredSize(new Dimension(200,50));
            optionView.add(Box.createHorizontalGlue());



        }

    }

    private void generateVronoiWorld(int numNodes, int width,int height) {
        ArrayList<Point> cp = new ArrayList<>(numNodes);

        for (int i = 0; i < numNodes; i++) {
            int x = (int) (width * Math.random());
            int y = (int) (height * Math.random());

            Point p = new Point(x,y);
            cp.add(p);
        }


        modelView.centerPoints =  cp;
        modelView.areas =  Voronoi.listOfCentresToPolygon(cp,new Dimension(width,height));;
        modelView.repaint();
    }


    private class ObjectSelectionView extends JPanel{
        public ObjectSelectionView(int prefaredWidth, int prefaredHeight) {
                Random r = new Random();
                this.setPreferredSize(new Dimension(prefaredWidth,prefaredHeight));
                this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

                ObjectSelectionView objectSelectionView = this;


                JRadioButton triange = new JRadioButton("tringle",false);
                JRadioButton square = new JRadioButton("square",true);
                JRadioButton pentagon = new JRadioButton("pentagon",false);

                ButtonGroup bg = new ButtonGroup();
                bg.add(triange);
                bg.add(square);
                bg.add(pentagon);

                objectSelectionView.add(triange);
                objectSelectionView.add(square);
                objectSelectionView.add(pentagon);
            //Set up color chooser for setting text color
            JColorChooser tcc = new JColorChooser();
            tcc.setVisible(true);
            objectSelectionView.add(tcc);
        }
    }

    private class FileView extends JPanel{
        public FileView(int prefaredWidth, int prefaredHeight) {
            Random r = new Random();
            this.setPreferredSize(new Dimension(prefaredWidth,prefaredHeight));
            this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

            JButton saveModel = new JButton("Save Map");
            JButton loadModel = new JButton("Load Map");

            this.add(saveModel);
            this.add(loadModel);

            saveModel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Map.saveToFile(map);
                }
            });


            loadModel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    map = Map.loadFile();
                    modelView.setMap(map);
                }
            });

        }
    }


}
