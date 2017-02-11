package nl.dke.pursuitevasion.gui.editor;

import nl.dke.pursuitevasion.gui.ModelView;

import javax.swing.*;
import java.awt.*;

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
    private ModelView modelView;

    public MapEditor(int prefaredWidht, int prefaredHeight) {
        this.setPreferredSize(new Dimension(prefaredWidht,prefaredHeight));
        this.setLayout(new BorderLayout(5,5));


        this.modelView = new ModelView(prefaredWidht*4/5,prefaredHeight);
        this.add(modelView,BorderLayout.CENTER);

        this.EastView = new JTabbedPane();


        this.optionView = new OptionView(prefaredWidht*1/5,prefaredHeight);

        EastView.add(optionView,"Level Generator");
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

        MapEditor mapEditor = new MapEditor(600,400);
        frame.add(mapEditor,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

    }

    private class OptionView extends JPanel {
        public OptionView(int prefaredWidth, int prefaredHeight) {
            this.setPreferredSize(new Dimension(prefaredWidth,prefaredHeight));
            this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
            OptionView optionView = this;




            JLabel l1 = new JLabel("Word1");
            SpinnerModel sm1 = new SpinnerNumberModel(0, 0, 9, 1); //default value,lower bound,upper bound,increment by
            JSpinner spinner1 = new JSpinner(sm1); spinner1.setPreferredSize(new Dimension(200,50));
            ((JSpinner.DefaultEditor)spinner1.getEditor()).getTextField().setColumns(10);
            spinner1.setMaximumSize(new Dimension(prefaredWidth,25));
            optionView.add(l1);
            l1.setLabelFor(spinner1);
            optionView.add(spinner1);


            JLabel l2 = new JLabel("Word2");
            JTextField text2 = new JTextField("21"); text2.setMaximumSize(new Dimension(prefaredWidth,25));
            optionView.add(l2);
            optionView.add(text2);

            JLabel l3 = new JLabel("Word3");
            SpinnerModel sm3 = new SpinnerNumberModel(0, 0, 9, 1); //default value,lower bound,upper bound,increment by
            JSpinner spinner3 = new JSpinner(sm3);
            optionView.add(l3);

            JLabel l4 = new JLabel("Word4");
            SpinnerModel sm4 = new SpinnerNumberModel(0, 0, 9, 1); //default value,lower bound,upper bound,increment by
            JSpinner spinner4 = new JSpinner(sm4);
            optionView.add(l4);

            JLabel l5 = new JLabel("Word5");
            SpinnerModel sm5 = new SpinnerNumberModel(0, 0, 9, 1); //default value,lower bound,upper bound,increment by
            JSpinner spinner5 = new JSpinner(sm5);
            optionView.add(l5);

          // optionView.add(new Swing.)

            JButton generate = new JButton("Generate Map");
            optionView.add(generate,Component.RIGHT_ALIGNMENT);
            optionView.add(Box.createHorizontalGlue());



        }
    }
}
