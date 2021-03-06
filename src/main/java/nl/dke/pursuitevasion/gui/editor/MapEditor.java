package nl.dke.pursuitevasion.gui.editor;

import nl.dke.pursuitevasion.gui.Voronoi;
import nl.dke.pursuitevasion.map.impl.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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


    public MapEditor(int preferredWidth, int preferredHeight) {
        this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        this.setLayout(new BorderLayout(5, 5));

        this.modelView = new ModelView(preferredWidth *4/5, preferredHeight,this);

        this.add(modelView,BorderLayout.CENTER);




        this.EastView = new JTabbedPane();
        EastView.setPreferredSize(new Dimension(preferredWidth *1/5, preferredHeight));
        this.optionView = new OptionView(preferredWidth *1/5, preferredHeight);

        this.objectSelectionView = new ObjectSelectionView(preferredWidth *1/5, preferredHeight);
        this.fileView = new FileView(preferredWidth *1/5, preferredHeight);

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
        public OptionView(int preferredWidth, int preferredHeight) {
            Random r = new Random();
            this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
            this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
            OptionView optionView = this;


            JLabel l2 = new JLabel("Name");
            JTextField text2 = new JTextField("Map" + r.nextInt(100)); text2.setMaximumSize(new Dimension(preferredWidth,25));
            optionView.add(l2); text2.setAlignmentX(Component.LEFT_ALIGNMENT);
            optionView.add(text2);

            JLabel l1 = new JLabel("Max Number Nodes");
            final SpinnerModel sm1 = new SpinnerNumberModel(48, 0, 1024, 12); //default value,lower bound,upper bound,increment by
            JSpinner spinner1 = new JSpinner(sm1);
            ((JSpinner.DefaultEditor)spinner1.getEditor()).getTextField().setColumns(10);
            spinner1.setMaximumSize(new Dimension(preferredWidth,25));
            optionView.add(l1); spinner1.setAlignmentX(Component.LEFT_ALIGNMENT);
            l1.setLabelFor(spinner1);
            optionView.add(spinner1);



            JLabel l3 = new JLabel("Blank");
            SpinnerModel sm3 = new SpinnerNumberModel(0, 0, modelView.getWidth(), 1); //default value,lower bound,upper bound,increment by
            JSpinner spinner3 = new JSpinner(sm3);
            ((JSpinner.DefaultEditor)spinner3.getEditor()).getTextField().setColumns(10);
            spinner3.setMaximumSize(new Dimension(preferredWidth,25));
            optionView.add(l3); spinner3.setAlignmentX(Component.LEFT_ALIGNMENT);
            l1.setLabelFor(spinner3);
            optionView.add(spinner3);

            JLabel l4 = new JLabel("Width");
            final SpinnerModel sm4 = new SpinnerNumberModel(600, 0, 1024, 9); //default value,lower bound,upper bound,increment by
            JSpinner spinner4 = new JSpinner(sm4);
            spinner4.setMaximumSize(new Dimension(preferredWidth,25));
            optionView.add(l4); spinner4.setAlignmentX(Component.LEFT_ALIGNMENT);
            optionView.add(spinner4);

            JLabel l5 = new JLabel("Height");
            final SpinnerModel sm5 = new SpinnerNumberModel(400, 0, 800, 9); //default value,lower bound,upper bound,increment by
            JSpinner spinner5 = new JSpinner(sm5); spinner5.setPreferredSize(new Dimension(200,50));
            spinner5.setMaximumSize(new Dimension(preferredWidth,25));
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

            JButton Finalize = new JButton("Finalize");
            Finalize.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    modelView.finalizeVoronoi();
                }
            });
            optionView.add(Finalize,Component.LEFT_ALIGNMENT); optionView.setPreferredSize(new Dimension(200,50));
            optionView.add(Box.createHorizontalGlue());



        }

    }

    private void generateVronoiWorld(int numNodes, int width,int height) {
        modelView.vronoiGeneration = true;

        ArrayList<Point> cp = new ArrayList<>(numNodes);

        for (int i = 0; i < numNodes; i++) {
            int x = (int) (width * Math.random());
            int y = (int) (height * Math.random());

            Point p = new Point(x,y);
            cp.add(p);
        }


Dimension d = new Dimension(width, height);
        HashMap<Point, Polygon> areas = Voronoi.listOfCentresToPolygon(cp, d);

        modelView.setAreas(cp,areas,d);
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

            triange.setAlignmentX(Component.LEFT_ALIGNMENT);
            square.setAlignmentX(Component.LEFT_ALIGNMENT);
            pentagon.setAlignmentX(Component.LEFT_ALIGNMENT);
                ButtonGroup bg = new ButtonGroup();
                bg.add(triange);
                bg.add(square);
                bg.add(pentagon);

               // objectSelectionView.add(triange);
               // objectSelectionView.add(square);
               // objectSelectionView.add(pentagon);
            //Set up color chooser for setting text color
            //JColorChooser tcc = new JColorChooser();
            //tcc.setVisible(true);
            //objectSelectionView.add(tcc);

            JToggleButton movePoint = new JToggleButton("Adjust Point");
            objectSelectionView.add(movePoint);
            movePoint.addActionListener(a -> setMovePointJToggleButton(a, movePoint));

            JButton unify = new JButton("Unify Points");
            objectSelectionView.add(unify);
            unify.addActionListener(a ->  unify());
            JButton simplify = new JButton("Simplyfy all");
            objectSelectionView.add(simplify);
            simplify.addActionListener(a ->  simplyfyAll());
            JButton undoSimplify = new JButton("Undo Simplyfy");
            objectSelectionView.add(undoSimplify);
            undoSimplify.addActionListener(a ->  undoSimplify());
            JButton kombinePolygones = new JButton("combine Polygones");
            objectSelectionView.add(kombinePolygones);
            kombinePolygones.addActionListener(a ->  kombinePolygones());


        }
    }

    private void kombinePolygones() {
        if (modelView == null) return;
        modelView.combinePolygones();
    }

    private void undoSimplify() {
        if (modelView == null) return;
        modelView.undoSimplyfyAll();
    }

    private void simplyfyAll() {
        if (modelView == null) return;
        modelView.simplyfyAll();
    }

    private void unify() {
        if (modelView == null) return;
        modelView.univfy();
    }

    private void setMovePointJToggleButton(ActionEvent a, JToggleButton movePoint) {
        if (modelView == null) return;
        modelView.setMovePointEnabled(movePoint.isSelected());

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
                    modelView.buildMap();
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
