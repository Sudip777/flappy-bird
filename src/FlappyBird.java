import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;
import java.io.*;

/**
 * FlappyBird class represents the main game panel and logic for the Flappy Bird game.
 */
public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    // Constants for board dimensions
    private final int boardWidth = 360;
    private final int boardHeight = 640;

    // Images
    private final Image backgroundImg;
    private final Image topPipeImg;
    private final Image bottomPipeImg;

    // Bird
    private final int birdX = boardWidth / 8;
    private final int birdY = boardHeight / 2;
    private final int birdW = 34;
    private final int birdH = 24;

    // Bird class representing the player
    private class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdW;
        int height = birdH;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Game Logic
    private final Bird bird;
    private int velocityY = 0;  // Vertical movement speed

    private ArrayList<Pipe> pipes;
    private final Random random = new Random();
    private final Timer gameLoop;
    private final Timer placePipesTimer;
    private boolean gameOver = false;
    private double score = 0;
    private int highScore = 0;  // Variable to store the high score

    // Pipes Logic
    private final int pipeX = boardWidth;
    private final int pipeY = 0;
    private final int pipeW = 64;  // Pipe width
    private final int pipeH = 512; // Pipe height

    // Pipe class representing the obstacles
    private class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeW;
        int height = pipeH;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    /**
     * Constructor for FlappyBird class.
     * Initializes the game and sets up timers.
     */
    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load Images
        backgroundImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./images/bg.png"))).getImage();
        Image birdImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./images/bird.png"))).getImage();
        topPipeImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./images/toppipe.png"))).getImage();
        bottomPipeImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./images/bottompipe.png"))).getImage();

        // Bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        // Load the high score
        loadHighScore();

        // Place pipes timer
        placePipesTimer = new Timer(1500, e -> placePipes());
        placePipesTimer.start();

        // Game loop timer
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    /**
     * Loads the high score from a file.
     */
    private void loadHighScore() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"));
            highScore = Integer.parseInt(reader.readLine());
            reader.close();
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading high score: " + e.getMessage());
        }
    }

    /**
     * Creates pipes and adds them to the game.
     */
    private void placePipes() {
        int randomPipeY = (int) (pipeY - pipeH / 4 - Math.random() * (pipeH / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeH + openingSpace;
        pipes.add(bottomPipe);
    }

    /**
     * Paints the game components on the panel.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
            * Draws the game components on the panel.
 */
    private void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        // Bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
        // Pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }
        // Score
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString("Score: " + (int) score, 10, 35);
        g.drawString("High Score: " + highScore, 10, 70);

        if (gameOver) {
            g.drawString("Game Over", 10, 100);
        }
    }

    /**
     * Moves the bird and pipes, checks for collisions, and updates the game state.
     */
    private void move() {
        // Bird movement
        // Gravity effect
        int gravity = 1;
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        // Pipe movement and collision detection
        for (Pipe pipe : pipes) {
            // Horizontal movement speed
            int velocityX = -4;
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // Game over logic
        if (bird.y > boardHeight) {
            gameOver = true;
        }

        // Update high score if needed
        if (score > highScore) {
            highScore = (int) score;
            saveHighScore();
        }
    }

    /**
     * Checks for collision between the bird and pipes.
     */
    private boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    /**
     * Saves the high score to a file.
     */
    private void saveHighScore() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"));
            writer.write(Integer.toString(highScore));
            writer.close();
        } catch (IOException e) {
            System.err.println("Error saving high score: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();

        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}

