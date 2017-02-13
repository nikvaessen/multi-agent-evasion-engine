import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JRadioButton;
import javax.swing.JButton;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JSlider;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.*;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSplitPane;

public class geschichteUbahnnetzMain extends JApplet {
	public geschichteUbahnnetzMain() {
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3845136919489774752L;
	/**
	 * 
	 */

	private JFrame frmGeschichteDesUbahnnetzes;
	JTextArea lblNewLabelTest;
	Karte mainScreen;
	JSlider slider;
	JScrollPane scrollPane;
	private String labelUbahnText = "";
	private int jahr;
	 
	
	/**
	 * Launch the application.
	 */
	 public void init() {
		//Execute a job on the event-dispatching thread; creating this applet's GUI.
	        try {
	            SwingUtilities.invokeAndWait(new Runnable() {
	                public void run() {
	                	//geschichteUbahnnetzMain window = new geschichteUbahnnetzMain();
	                	initialize();
						setVisible(true);
						//JOptionPane.showMessageDialog(null, "My Goodness, this is so concise");
	                }
	            });
	        } catch (Exception e) {
	        	JOptionPane.showMessageDialog(null, "My Goodness, this is so concise2");
	        	//System.err.println("createGUI didn't complete successfully");
	            //System.err.println("createGUI didn't complete successfully");
	        }
	}

	
	/**
	 * Create the application.
	 */
	//public geschichteUbahnnetzMain() {
		//initialize();
		
//		public void paintComponent(Graphics g2) {
//
//			super.paintComponent(g2);
//			Graphics2D g = (Graphics2D) g2;
//			g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
//		public WorldLabelDrawer(String BackgroundPicuturelocation) {
//			try {
//				image = javax.imageio.ImageIO.read(new File(
//						BackgroundPicuturelocation));
//			} catch (Exception e) { /* handled in paintComponent() */
//			}
	//}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		//getContentPane().setTitle("Geschichte des Ubahnnetzes M\u00FCnchens");
		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
		borderLayout.setVgap(12);
		borderLayout.setHgap(7);
		getContentPane().setBounds(100, 100, 515, 548);
		//frmGeschichteDesUbahnnetzes.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		mainScreen = new Karte(){
			
			
		};
		mainScreen.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent arg0) {
				kartenMausZiehen(arg0);
			}
		});
		mainScreen.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				karteMausrad(arg0);
			}
		});
		mainScreen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				kartenKlick(arg0);
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
				karteMauspressed(arg0);
			}
		});
		
		
		mainScreen.setPreferredSize(new Dimension(400, 200));
		mainScreen.setMinimumSize(new Dimension(400, 200));
		mainScreen.setBounds(new Rectangle(0, 0, 600, 400));
		getContentPane().add(mainScreen, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		 scrollPane = new JScrollPane();
		panel_1.add(scrollPane);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		lblNewLabelTest = new JTextArea("Jahr: " + jahr);
		lblNewLabelTest.setLineWrap(true);
		scrollPane.setViewportView(lblNewLabelTest);
		lblNewLabelTest.setMaximumSize(new Dimension(46, 400));
		lblNewLabelTest.setPreferredSize(new Dimension(46, 400));
		
		slider = new JSlider();
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				sliderChanced();
			}
		});
		
		slider.setMajorTickSpacing(15);
		slider.setMinorTickSpacing(5);
		slider.setMinimum(1972);
		slider.setMaximum(2013);
		slider.setPreferredSize(new Dimension(200, 16));
		slider.setAlignmentX(1.0f);
		getContentPane().add(slider, BorderLayout.SOUTH);
		
		
		panel_1.setPreferredSize(new Dimension(200, 600));
		panel_1.setMaximumSize(new Dimension(200, 600));
		getContentPane().add(panel_1, BorderLayout.WEST);
		panel_1.setLayout(new GridLayout(0, 1, 2, 0));
		
		
		
		
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2);
		panel_2.setLayout(new GridLayout(0, 2, 0, 0));
		checkbox(1);
		checkbox(2);
		checkbox(3);
		
		JCheckBox checkU4 = new JCheckBox("U4");
		checkU4.setSelected(true);
		checkbox(4);
		checkU4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkbox(4);
			}
		});
		
		JCheckBox checkU2 = new JCheckBox("U2");
		checkU2.setSelected(true);
		checkU2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkbox(2);
			}
		});
		
		JCheckBox checkU1 = new JCheckBox("U1");
		checkU1.setSelected(true);
		checkU1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkbox(1);
			}
		});
		panel_2.add(checkU1);
		panel_2.add(checkU2);
		
		JCheckBox checkU3 = new JCheckBox("U3");
		checkU3.setSelected(true);
		checkU3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkbox(3);
			}
		});
		panel_2.add(checkU3);
		panel_2.add(checkU4);
		
		JCheckBox checkU5 = new JCheckBox("U5");
		checkU5.setSelected(true);
		checkbox(5);
		checkU5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkbox(5);
			}
		});
		panel_2.add(checkU5);
		
		JCheckBox checkU6 = new JCheckBox("U6");
		checkU6.setSelected(true);
		checkbox(6);
		checkU6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkbox(6);
			}
		});
		panel_2.add(checkU6);
		
		JCheckBox checkU7 = new JCheckBox("U7");
		checkU7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkbox(7);
			}
		});
		panel_2.add(checkU7);
		
		JCheckBox chckbxPostubahn = new JCheckBox("Postubahn");
		chckbxPostubahn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkbox(0);
			}
		});
		panel_2.add(chckbxPostubahn);
		
		JCheckBox chckbxRohrpost = new JCheckBox("Rohrpost 1933");
		chckbxRohrpost.setVisible(false);
		chckbxRohrpost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkbox(8);
			}
		});
		panel_2.add(chckbxRohrpost);
		
		JCheckBox chckbxRohrpost_1 = new JCheckBox("Rohrpost1956");
		chckbxRohrpost_1.setVisible(false);
		chckbxRohrpost_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkbox(9);
			}
		});
		panel_2.add(chckbxRohrpost_1);
		
		JLabel label_1 = new JLabel("");
		panel_1.add(label_1);
	}


	protected void checkbox(int i) {
		mainScreen.switchAuswahl(i);
		mainScreen.repaint();
	}


	protected void karteMauspressed(MouseEvent arg0) {
		mainScreen.mousePressed(arg0);
		
	}


	protected void kartenMausZiehen(MouseEvent arg0) {
		System.out.println(arg0.getButton());
		mainScreen.mouseDragged(arg0);
		mainScreen.repaint();
	}


	protected void karteMausrad(MouseWheelEvent arg0) {
		mainScreen.zoomAendern(arg0.getWheelRotation());
		System.out.println(arg0.getWheelRotation());
		mainScreen.repaint();
	}


	protected void kartenKlick(MouseEvent arg0) {
		if (arg0.getClickCount() == 2) {
			mainScreen.setCenter(arg0.getPoint());
			System.out.println("doppelklick");
			mainScreen.repaint();
			return;
		}
		
		Vector<Ubahnstation> ubs = mainScreen.getUbahnFromBildschirmKoordinate(arg0.getPoint());
		Koordinate k = mainScreen.getKoordinateFromBildschirmKoordinate(arg0.getPoint());
		StringSelection ss = new StringSelection(k.toString());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		System.out.println("In Zwischenablage gespeichert: " + k.toString());
		labelUbahnText  = new String();
		for (Ubahnstation ub : ubs) {
			labelUbahnText += ub.toString() + "\n";
		}
		
		refreshLabelUbahnText();
		
	}


	private void refreshLabelUbahnText() {
		
		
		lblNewLabelTest.setText("Jahr: " + jahr + "\n" + labelUbahnText);
		scrollPane.getVerticalScrollBar().setValue(0);
		//getViewport().setVa.setViewPosition(new Point(0,0));
		
		
	}


	protected void sliderChanced() {
		 jahr = slider.getValue();
		mainScreen.setJahreszahl(jahr);
		System.out.print(jahr);
		refreshLabelUbahnText();
		mainScreen.repaint();
		
	}

}
