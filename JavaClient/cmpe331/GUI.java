package cmpe331;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.OutputStream;
import java.io.PrintStream;

public class GUI extends JFrame {

    private JTextArea terminalArea;
    private boolean terminalVisible = false;
    private JPanel appointeeListPanel;
    
    JPanel mainPanel;
    
    JFrame terminalFrame;
	private int lastClickedIndex;
	private int queueLength = 0;

    public GUI() {
        setTitle("Appointment System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);

        createMenuBar();
        createMainPanel();


        // Set a background color
        getContentPane().setBackground(Color.CYAN);

        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem connectionItem = new JMenuItem("Connection Parameters");

        connectionItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openConnectionSettings();
            }
        });

        settingsMenu.add(connectionItem);
        menuBar.add(settingsMenu);

        setJMenuBar(menuBar);

        menuBar.setForeground(Color.lightGray);
    }

    private void openConnectionSettings() {
        // Add code to open the window for connection parameters
        // You can use a JOptionPane or create a new JFrame for this purpose
        JOptionPane.showMessageDialog(this, "Connection Settings");
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Buttons on top
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(20));

		buttonPanel.setPreferredSize(new Dimension(100, 100));

        JButton nextPersonButton = new JButton("Next Person");
        JButton openDoorButton = new JButton("Open Door");
        JButton moveUpButton = new JButton("Move Up");
        JButton moveDownButton = new JButton("Move Down");
        JButton removeButton = new JButton("Remove");
        JButton addButton = new JButton("Add Card");
        JButton refreshButton = new JButton("Refresh");
        
        nextPersonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (Main.getGuestList().length > 0 ) {
            	    int numRows = Main.getGuestList().length;

            	    // Create a new array with one less row
            	    String[][] temp = new String[numRows - 1][3];

            	    // Copy elements before the removed row
            	    for (int i = 0; i < 0; i++) {
            	        temp[i] = Main.getGuestList()[i];
            	    }

            	    // Copy elements after the removed row
            	    for (int i = 0 + 1; i < numRows; i++) {
            	        temp[i - 1] = Main.getGuestList()[i];
            	    }

            	    Main.setGuestList(temp);
            	    if(lastClickedIndex > 0) {
            	    	lastClickedIndex--;
            	    }
            	    Main.deleteFirst();
            	    Main.arduinoSend("ard::next_person<" + Main.getGuestList()[0][1]);
            	}

            	
            	createAppointeeListPanel();
            	repaint();

            }
        });
        
        openDoorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(lastClickedIndex > 0 && lastClickedIndex < Main.getGuestList().length) {
            		Main.arduinoSend("ard::open_door");
            		System.out.println("Door opened by button press");
            	}
            	createAppointeeListPanel();
            	repaint();

            }
        });
        
        moveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(lastClickedIndex > 0 && lastClickedIndex < Main.getGuestList().length) {
            		String[][] temp = new String[Main.getGuestList().length][3];
            		temp = Main.getGuestList().clone();
            	    String[] tempRow = temp[lastClickedIndex];
            	    temp[lastClickedIndex] = temp[lastClickedIndex - 1];
            	    temp[lastClickedIndex - 1] = tempRow;
            		Main.setGuestList(temp);
            		lastClickedIndex --;
            		Main.arduinoSend("ard::next_person<" + Main.getGuestList()[0][1]);
            	}
            	createAppointeeListPanel();
            	repaint();

            }
        });
        
        moveDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(lastClickedIndex >= 0 && lastClickedIndex < Main.getGuestList().length - 1) {
            		String[][] temp = new String[Main.getGuestList().length][3];
            		temp = Main.getGuestList().clone();
            	    String[] tempRow = temp[lastClickedIndex];
            	    temp[lastClickedIndex] = temp[lastClickedIndex + 1];
            	    temp[lastClickedIndex + 1] = tempRow;
            		Main.setGuestList(temp);
            		lastClickedIndex++;
            		Main.arduinoSend("ard::next_person<" + Main.getGuestList()[0][1]);
            	}
            	createAppointeeListPanel();
            	repaint();

            }
        });
        
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (Main.getGuestList().length > 0 && lastClickedIndex >= 0 && lastClickedIndex < Main.getGuestList().length) {
            	    int numRows = Main.getGuestList().length;

            	    // Create a new array with one less row
            	    String[][] temp = new String[numRows - 1][3];

            	    // Copy elements before the removed row
            	    for (int i = 0; i < lastClickedIndex; i++) {
            	        temp[i] = Main.getGuestList()[i];
            	    }

            	    // Copy elements after the removed row
            	    for (int i = lastClickedIndex + 1; i < numRows; i++) {
            	        temp[i - 1] = Main.getGuestList()[i];
            	    }

            	    Main.setGuestList(temp);
            	    Main.arduinoSend("ard::next_person<" + Main.getGuestList()[0][1]);
            	    if(lastClickedIndex > 0) {
            	    	lastClickedIndex--;
            	    }
            	}

            	
            	createAppointeeListPanel();
            	repaint();
            }
        });
        
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Main.addCards();
            	createAppointeeListPanel();
            	repaint();
            	Main.arduinoSend("ard::next_person<" + Main.getGuestList()[0][1]);
            	
            }
        });
        
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
            	Main.resetGuestList();
            	
            	createAppointeeListPanel();
            	repaint();
            	
            	Main.arduinoSend("ard::next_person<" + Main.getGuestList()[0][1]);
            }
        });

        buttonPanel.setForeground(Color.ORANGE);

        buttonPanel.add(nextPersonButton);
        buttonPanel.add(openDoorButton);
        buttonPanel.add(moveUpButton);
        buttonPanel.add(moveDownButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        appointeeListPanel = new JPanel();
        mainPanel.add(appointeeListPanel, BorderLayout.CENTER);
        createAppointeeListPanel();


        mainPanel.setBackground(Color.blue);

        add(mainPanel, BorderLayout.CENTER);
    }
    
    void createAppointeeListPanel(){
    	appointeeListPanel.removeAll();
        appointeeListPanel.setLayout(new GridLayout(8, 1));
        
        
        
        if(Main.getGuestList() != null) {
        	queueLength = Main.getGuestList().length;
        	
        }
        else {
        	queueLength = 0;
        }
        for (int i = 0; i <  queueLength; i++) {
            JPanel appointeePanel = new JPanel();
            appointeePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

            // Customize this panel to display individual appointee information
            // You may use JLabels or other components to display data
            JLabel nameLabel = new JLabel("Name " + Main.getGuestList()[i][0]);
            JLabel uidLabel = new JLabel("UID: " + Main.getGuestList()[i][1]);
            JLabel timeLabel = new JLabel("Time: " + Main.getGuestList()[i][2]);

            appointeePanel.add(nameLabel);
            appointeePanel.add(uidLabel);
            appointeePanel.add(timeLabel);
            
            // Add a MouseListener to each appointeePanel
            final int appointeeIndex = i;
            appointeePanel.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Update the last clicked index
                    lastClickedIndex = appointeeIndex;

                    // Update the GUI to reflect the selection
                    createAppointeeListPanel();
                }
            });
            if (i == lastClickedIndex) {
                appointeePanel.setBackground(Color.YELLOW);
            }

            appointeeListPanel.add(appointeePanel);
        }

        appointeeListPanel.revalidate();
        appointeeListPanel.repaint();
    }


//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new GUI());
//    }
    
}
