package org.vit.javaproject.pacman;

// importing all packages required for this project
import java.awt.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.Timer;

// JPanel - to get a window
// ActionListener - for the code to react to buttons
// extends - Inheritence
public class GameModel extends JPanel implements ActionListener {

    // dimension of the playing field
	private Dimension d;
    private final Font gameFont = new Font("Helvetica", Font.BOLD, 16);
    // checks if the game is running
    private boolean inGame = false;
    // checks if pacman is alive or not
    private boolean knockout = false;

    // constant varaibles
    private final int BLOCK_SIZE = 24;
    private final int NO_BLOCKS = 15;
    private final int SCREEN_SIZE = NO_BLOCKS * BLOCK_SIZE;
    private final int MAX_GHOSTS = 12;
    private final int SPEED_PACMAN = 3;

    private int NO_GHOSTS = 6;
    private int lives, score;

    // x and y positions in the game 
    private int[] dx, dy;
    // no of ghosts, position of ghosts and speed
    private int[] x_ghost, y_ghost, dx_ghost, dy_ghost, ghostSpeed;

    // images for lives and ghosts
    private Image heart, ghost;
    private Image up_pacman, down_pacman, left_pacman, right_pacman;

    /**
     * first 2: x and y coordinates
     * last 2: delta changes in horizontal and vertical directions
     */

    private int x_pacman, y_pacman, dx_pacman, dy_pacman;
    // used in TAdapter class extends KeyAdapter{}
    // can be accessed using arrow keys on the keyboard
    private int dx_req, dy_req;

        
    /** CREATING THE PACMAN SCREEN: no_blocks is 15
     * 225 elements here represent the 225 possible positions in the game
     * 15 rows and 15 columns, each number indicates the specific element to be displayed
     * 0: inidcates the purple obstcales in the game
     * 1,2,4,8: left, top, right and bottom border
     * 16: white dots that pacman collects
     * WE CAN ADD THESE NUMBERS:
     * 19: Top left white dot = 1 + 2 + 16
     * White dots in the middle (not near the obstacles or borders): 16
     */
    private final short levelData[] = {
    	19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
        17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
        0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
        19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
        17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
        21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
        17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    // Takes screendata of each level of the game
    private short[] screenData;
    private Timer timer;

    // Calling the constructor
    public GameModel() {

        loadImageIcons();
        initVariables();
        addKeyListener(new TAdapter());
        // focus for window
        setFocusable(true);
        // to start the game
        initGame();
    }
    
    // load images
    private void loadImageIcons() { 
        //Path of images 
        String PATH  = "C:\\Users\\anann\\OneDrive\\Desktop\\College\\SEMESTERS\\SEM 6\\CSE1007 Java\\javaws\\Pacman\\src\\images";

    	down_pacman = new ImageIcon(PATH + "\\down.gif").getImage();
    	up_pacman = new ImageIcon(PATH + "\\up.gif").getImage();
    	left_pacman = new ImageIcon(PATH + "\\left.gif").getImage();
    	right_pacman = new ImageIcon(PATH + "\\right.gif").getImage();
        ghost = new ImageIcon(PATH + "\\ghost.gif").getImage();
        heart = new ImageIcon(PATH + "\\heart.png").getImage();

    }
    // initialize variables
        private void initVariables() {
        // screendata is an array of 225 elements (15*15)
        screenData = new short[NO_BLOCKS * NO_BLOCKS];
        d = new Dimension(400, 400);
        // array of max no of ghosts allowed
        x_ghost = new int[MAX_GHOSTS];
        dx_ghost = new int[MAX_GHOSTS];
        y_ghost = new int[MAX_GHOSTS];
        dy_ghost = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];

        // array to hold x and y positions
        dx = new int[4];
        dy = new int[4];
        
        /** timer: takes care of animation: FPS of the images
         * the game is re-drawn every 40 millisecond
         * greater the time: slower the speed of the game
         * this: speed of the pacman and ghosts
        */
        timer = new Timer(40, this);
        timer.start();
    }

    // For initializing gameplay methods
    private void gamePlay(Graphics2D g2d) {

        if (knockout) {

            dead();

        } else {

            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    // display intro on game screen
    private void IntroScreen(Graphics2D g2d) {
 
    	String start = "Press SPACE to start";
        g2d.setColor(new Color(255,215,0));
        // position the text
        g2d.drawString(start, (SCREEN_SIZE)/4, 150);
    }

    // display score on game screen
    private void drawScore(Graphics2D g) {
        g.setFont(gameFont);
        g.setColor(new Color(64,224,208));
        String sc = "Score: " + score;
        // position text on game screen
        g.drawString(sc, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        // check how many lives left to display the number of hearts    
        for (int i = 0; i < lives; i++) {
            g.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    // check if there are any points for the pacman to eat
    private void checkMaze() {

        int i = 0;
        boolean gameFinished = true;

        while (i < NO_BLOCKS * NO_BLOCKS && gameFinished) {

            if ((screenData[i]) != 0) {
                gameFinished = false;
            }

            i++;
        }

        // move up to the next level if the game is finished
        // but for this game, we simply restart
        if (gameFinished) {

            score += 50;

            if (NO_GHOSTS < MAX_GHOSTS) {
                NO_GHOSTS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }
            // restart the game to initial level
            initLevel();
        }
    }

    // how to check if pacman is dead
    // decrase it's life everytime it dies
    private void dead() {

    	lives--;

        if (lives == 0) {
            inGame = false;
        }

        continueLevel();
    }

    private void moveGhosts(Graphics2D g2d) {

        int pos;
        int count;

        for (int i = 0; i < NO_GHOSTS; i++) {
            // the ghosts move one square and decide if they change the direction
            if (x_ghost[i] % BLOCK_SIZE == 0 && y_ghost[i] % BLOCK_SIZE == 0) {
                 // determines where the ghost is located
                pos = x_ghost[i] / BLOCK_SIZE + NO_BLOCKS * (int) (y_ghost[i] / BLOCK_SIZE);

                count = 0;
                // ghosts cannot move across borders (1, 2, 4, 8)
                if ((screenData[pos] & 1) == 0 && dx_ghost[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && dy_ghost[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && dx_ghost[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && dy_ghost[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                /**
                 * if the ghost enters a tunnel, it will move in the same direction
                 * until it is out of the tunnel.
                */
                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        dx_ghost[i] = 0;
                        dy_ghost[i] = 0;
                    } else {
                        dx_ghost[i] = -dx_ghost[i];
                        dy_ghost[i] = -dy_ghost[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }
                    
                    // update the speed of the ghost and draw the ghost
                    dx_ghost[i] = dx[count];
                    dy_ghost[i] = dy[count];
                }

            }

            x_ghost[i] = x_ghost[i] + (dx_ghost[i] * ghostSpeed[i]);
            y_ghost[i] = y_ghost[i] + (dy_ghost[i] * ghostSpeed[i]);
            drawGhost(g2d, x_ghost[i] + 1, y_ghost[i] + 1);

            // if there is a collision between ghosts and pacman, pacman dies
            if (x_pacman > (x_ghost[i] - 12) && x_pacman < (x_ghost[i] + 12)
                    && y_pacman > (y_ghost[i] - 12) && y_pacman < (y_ghost[i] + 12)
                    && inGame) {

                knockout = true;
            }
        }
    }

    // for drawing the ghost
    private void drawGhost(Graphics2D g2d, int x, int y) {
    	g2d.drawImage(ghost, x, y, this);
        }

    private void movePacman() {

        int pos;
        short ch;
        // pacman can move one square and then decide to change directions
        if (x_pacman % BLOCK_SIZE == 0 && y_pacman % BLOCK_SIZE == 0) {
            pos = x_pacman / BLOCK_SIZE + NO_BLOCKS * (int) (y_pacman / BLOCK_SIZE);
            // ch is the current position of pacman on screen
            ch = screenData[pos];

            // 16 is the points pacman can eat: white
            // score increased by 1 if pacman is eating white dots
            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }

             // control pacman with dx_req and dy_req with cursor keys
            if (dx_req != 0 || dy_req != 0) {
                // checks if pacman has encountered the border or not
                if (!((dx_req == -1 && dy_req == 0 && (ch & 1) != 0)
                        || (dx_req == 1 && dy_req == 0 && (ch & 4) != 0)
                        || (dx_req == 0 && dy_req == -1 && (ch & 2) != 0)
                        || (dx_req == 0 && dy_req == 1 && (ch & 8) != 0))) {

                            // update the position of pacman
                    dx_pacman = dx_req;
                    dy_pacman = dy_req;
                }
            }

            // Check for stanstill: pacman stops if he cannot move further in his current direction
            if ((dx_pacman == -1 && dy_pacman == 0 && (ch & 1) != 0)
                    || (dx_pacman == 1 && dy_pacman == 0 && (ch & 4) != 0)
                    || (dx_pacman == 0 && dy_pacman == -1 && (ch & 2) != 0)
                    || (dx_pacman == 0 && dy_pacman == 1 && (ch & 8) != 0)) {
                dx_pacman = 0;
                dy_pacman = 0;
            }
        } 
        // position is adjusted according to speed of pacman
        x_pacman = x_pacman + SPEED_PACMAN * dx_pacman;
        y_pacman = y_pacman + SPEED_PACMAN * dy_pacman;
    }

        /**
     * There are four possible directions for a Pacman. 
     * There are four images for all directions. The images are used to 
     * animate Pacman opening and closing his mouth.
     */

    private void drawPacman(Graphics2D g2d) {
        // checks which cursor button was pressed
        if (dx_req == -1) {
            // if left arrow pressed, left image is loaded
        	g2d.drawImage(left_pacman, x_pacman + 1, y_pacman + 1, this);
        } else if (dx_req == 1) {
        	g2d.drawImage(right_pacman, x_pacman + 1, y_pacman + 1, this);
        } else if (dy_req == -1) {
        	g2d.drawImage(up_pacman, x_pacman + 1, y_pacman + 1, this);
        } else {
        	g2d.drawImage(down_pacman, x_pacman + 1, y_pacman + 1, this);
        }
    }

    // game is drawn with 225 numbers
    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        // iterate through the rows and columns of the array
        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                // set color and stroke of blocks
                g2d.setColor(new Color(148,0,211));
                g2d.setStroke(new BasicStroke(5));
                
                // green blocks are made using rectangles
                if ((levelData[i] == 0)) { 
                	g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                 }

                // draw lines for all the borders
                if ((screenData[i] & 1) != 0) { 
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) { 
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) { 
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) { 
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) { 
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x + 10, y + 10, 6, 6);
               }

                i++;
            }
        }
    }

    // initialize the game
    private void initGame() {

    	lives = 3;
        score = 0;
        initLevel(); //level of the game initialized
        NO_GHOSTS = 6;
        currentSpeed = 3;
    }

    // initialize the level of the game
    private void initLevel() {

        int i;
        for (i = 0; i < NO_BLOCKS * NO_BLOCKS; i++) {
            // copy the playfield of levelData to screenData
            screenData[i] = levelData[i];
        }

        continueLevel();
    }

    // defines the positions of the ghosts/pacman and creates random ghost speeds
    private void continueLevel() {

    	int dx = 1;
        int random;

        for (int i = 0; i < NO_GHOSTS; i++) {

            y_ghost[i] = 4 * BLOCK_SIZE; //start position
            x_ghost[i] = 4 * BLOCK_SIZE;
            dy_ghost[i] = 0;
            dx_ghost[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        // define the start position of pacman
        x_pacman = 7 * BLOCK_SIZE;  //start position
        y_pacman = 11 * BLOCK_SIZE;
        dx_pacman = 0;	//reset direction move
        dy_pacman = 0;
        dx_req = 0;		// reset direction controls
        dy_req = 0;
        knockout = false;
    }

    // For visuals
    public void paintComponent(Graphics g) {
        // constructor of the parent class called
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // set bg color
        g2d.setColor(Color.black);
        // draw positions (rectangle for better visualization)
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (inGame) {
            gamePlay(g2d);
        } else {
            IntroScreen(g2d);
        }
        // used to bind all the components
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }


    // Controller class for receiving keyboard events on pressing
    class TAdapter extends KeyAdapter {

        
        // Event which inidcates that a key has been pressed
        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            // if the user is in the game
            if (inGame) {
                // if the pressed key is left arrow
                if (key == KeyEvent.VK_LEFT) {
                    dx_req = -1;
                    dy_req = 0;
                } 
                // if the pressed key is right arrow
                else if (key == KeyEvent.VK_RIGHT) {
                    dx_req = 1;
                    dy_req = 0;
                } 
                // if the pressed key is up arrow
                else if (key == KeyEvent.VK_UP) {
                    dx_req = 0;
                    dy_req = -1;
                } 
                // if the pressed key is down arrow
                else if (key == KeyEvent.VK_DOWN) {
                    dx_req = 0;
                    dy_req = 1;
                } 
                // to escape the game when game is running
                else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                } 
            } 
            // Press SPACE to start the game
            else {
                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    initGame();
                }
            }
        }
}

    // ActionListener interface gets this ActionEvent when the event occurs
    // Eg: Event like a button pressed  
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }	
	}