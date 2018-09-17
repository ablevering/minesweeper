package board;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class GUI extends JFrame {

    private ArrayList<JPanel> squares = new ArrayList<JPanel>();
    private JPanel board;
    private BoardManager bm = null;
    private Timer timer = new Timer();
    private JOptionPane winScreen;
    private int width;
    private int height;
    private int mineCount;
    private int flags = 0;
    public int time = -1;

    public static void main(String[] args) {
        new GUI(10, 10, 15);
    }

    public GUI(int width, int height, int mines) {
        this.width = width;
        this.height = height;
        this.mineCount = mines;

        JToolBar toolBar = new JToolBar();
        JLabel xLabel = new JLabel("X:");
        JTextField xText = new JTextField("10");
        JLabel yLabel = new JLabel("Y:");
        JTextField yText = new JTextField("10");
        JLabel mineLabel = new JLabel("Mines:");
        JTextField mineText = new JTextField("15");
        JButton newGameButton = new JButton("New Game");

        xText.getCaret().setBlinkRate(0);
        yText.getCaret().setBlinkRate(0);
        mineText.getCaret().setBlinkRate(0);

        toolBar.add(xLabel);
        toolBar.add(xText);
        toolBar.add(yLabel);
        toolBar.add(yText);
        toolBar.add(mineLabel);
        toolBar.add(mineText);
        toolBar.add(newGameButton);

        getContentPane().add(toolBar, BorderLayout.NORTH);

        JLabel minesLeftLabel = new JLabel("Mines Left: ");
        JLabel spacer = new JLabel("                            ");
        JLabel timeCounter = new JLabel("Time: ");
        
        JPanel southPanel = new JPanel();
        southPanel.add(minesLeftLabel, BorderLayout.WEST);
        southPanel.add(spacer, BorderLayout.CENTER);
        southPanel.add(timeCounter, BorderLayout.EAST);

        getContentPane().add(southPanel, BorderLayout.SOUTH);

        // Create a new game and dispose of the old one
        newGameButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                try {
                    int x = Integer.parseInt(xText.getText());
                    int y = Integer.parseInt(yText.getText());
                    int mines = Integer.parseInt(mineText.getText());
                    if (x < 1 || y < 1 || mines < 1 || x != y)
                        throw (new NumberFormatException());
                    new GUI(x, y, mines);
                    setVisible(false);
                    dispose();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(GUI.this, "X, Y, and Mines must all be integers greater than zero.\nX and Y must also be equal to each other.");
                }
            }
        });
        
        // Build the timer, set it to increment by 1 every second
        class Tick extends TimerTask{
            public void run() {
                time ++;
                timeCounter.setText("Time: " + time);
            }
        }
        timer.schedule(new Tick(), 0, 1000);
        
        // Create the board
        board = new JPanel();
        getContentPane().add(board);
        board.setLayout(new GridLayout(width, height));

        Dimension dim = new Dimension(30 * width, 30 * height);
        board.setPreferredSize(dim);

        // Create the board's tiles
        for (int i = 0; i < width * height; i++) {
            JPanel square = new JPanel();
            square.setBackground(Color.LIGHT_GRAY);
            square.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            board.add(square);
            squares.add(square);
        }

        // Give each tile a mouseListener. 
        for (JPanel square : squares) {
            square.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent me) {
                    // Check for left or right mouse
                    if (me.getButton() == MouseEvent.BUTTON1)
                        parseLeftClick(me);
                    if (me.getButton() == MouseEvent.BUTTON3)
                        parseRightClick(me);
                }

                private void parseRightClick(MouseEvent me) {
                    JPanel square = (JPanel) me.getSource();
                    if (square.getBackground() != Color.RED) {
                        square.setBackground(Color.RED);
                        flags++;
                        
                        // If the board isn't set up yet, do nothing
                        if (bm == null) {
                            return;
                        }

                        // Check to see if the user has won!
                        if (bm.getMineSet().size() == flags) {
                            for (int m : bm.getMineSet()) {
                                if (squares.get(m).getBackground() != Color.RED) {
                                    return;
                                }
                            }
                            // The user won- each mine was flagged. Display the win screen
                            timer.cancel();
                            minesLeftLabel.setText("Mines Left: " + (bm.getMineSet().size() - flags));
                            JOptionPane.showMessageDialog(winScreen,
                                    "You win! \nTime: "+time + "\nMines: " + flags,
                                    "Bravo!",
                                    JOptionPane.PLAIN_MESSAGE);
                        }
                    } else {
                        // The tile was already flagged. The user wants to disable the flag.
                        square.setBackground(Color.LIGHT_GRAY);
                        flags--;
                    }
                    // Update the minesLeftPanel.
                    if (bm != null)
                        minesLeftLabel.setText("Mines Left: " + (bm.getMineSet().size() - flags));

                }

                private void parseLeftClick(MouseEvent me) {
                    JPanel square = (JPanel) me.getSource();
                    int location = getTileLocation(square);
                    
                    // Check for a flag
                    if (square.getBackground() != Color.RED) {
                        // Set up the board if not done already
                        if (bm == null)
                            bm = new BoardManager(mineCount, location, width, height);
                        
                        // Update the minesLeftPanel:
                        minesLeftLabel.setText("Mines Left: " + (bm.getMineSet().size() - flags));
                        
                        // Draw the borders
                        Graphics g = square.getGraphics();
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, square.getWidth(), square.getHeight());
                        g.setColor(Color.WHITE);
                        g.fillRect(1, 1, square.getWidth()-2, square.getHeight()-2);
                        
                        Graphics g2D;
                        int i = bm.revealTile(location);
                        // Check if the tile clicked is a mine. 
                        if (i == -1) {
                            // It is a mine! Play the mine explosion animation and launch the 
                            // lose screen.
                            timer.cancel();
                            JPanel s = squares.get(location);
                            g2D = (Graphics2D) s.getGraphics();
                            g2D.setColor(Color.BLACK);
                            g2D.fill3DRect(0, 0, s.getWidth(), s.getHeight(), false);
                            for (int m : bm.getMineSet()) {
                                s = squares.get(m);
                                g2D = (Graphics2D) s.getGraphics();
                                g2D.setColor(Color.BLACK);
                                g2D.fill3DRect(0, 0, s.getWidth(), s.getHeight(), false);
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            
                            JOptionPane.showMessageDialog(winScreen,
                                    "You clicked a mine! \nTime: "+time + "\nMines: " + bm.getMineSet().size(),
                                    "Defeat",
                                    JOptionPane.PLAIN_MESSAGE);
                        }
                        
                        g2D = (Graphics2D) squares.get(location).getGraphics();
                        // If the tile is adjacent to no mines, we need to launch the cascade
                        // algorithm and clear away all adjacent non-mines.
                        if (i == 0) {
                            bm.getAdjacentZeroes(location%width,location/height);
                            for (int clear : bm.getClears()) {
                                // Build the tile border for each cleared tile
                                g = squares.get(clear).getGraphics();
                                g.setColor(Color.BLACK);
                                g.fillRect(0, 0, square.getWidth(), square.getHeight());
                                g.setColor(Color.WHITE);
                                g.fillRect(1, 1, square.getWidth()-2, square.getHeight()-2);
                                if (bm.revealTile(clear) > 0) {
                                    // If the tile isn't a zero, we want to display its value
                                    g2D = (Graphics2D) squares.get(clear).getGraphics();
                                    g2D.drawString(String.valueOf(bm.revealTile(clear)), 12, 19);
                                }
                            }
                            return;
                        }
                        // The tile clicked is a standard number. Reveal it
                        g2D.drawString(String.valueOf(i), 12, 19);
                    }
                }
            });
        }

        // Necessary JFrame management. The order of these statements matter- don't change.
        this.setTitle("MineSweeper");
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
        this.setResizable(false);
        setVisible(true);
    }

    private int getTileLocation(JPanel square) {
        for (int i = 0; i < width * height; i++) {
            if (squares.get(i) == square) {
                return i;
            }
        }
        return -1;
    }
}