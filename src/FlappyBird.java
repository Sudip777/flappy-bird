import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdW = 34;
    int birdH = 24;

    class Bird {
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
    Bird bird;
    int velocityX = -4; // This moves the pipes to the left speed (simulating bird moving right)
    int velocityY = 0; // up and down bird movement
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();
    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;


    // Pipes Logic
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeW = 64;  //scaled by 1/6
    int pipeH = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeW;
        int height = pipeH;
        Image img;
        boolean passed = false;

        Pipe(Image img){
            this.img = img;
        }

    }

    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth,boardHeight));
//        setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this);

        // Load Images
        backgroundImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./images/bg.png"))).getImage();
        birdImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./images/bird.png"))).getImage();
        topPipeImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./images/toppipe.png"))).getImage();
        bottomPipeImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./images/bottompipe.png"))).getImage();

        // bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        // place pipes timer
        placePipesTimer = new Timer(1500, e -> placePipes());

        placePipesTimer.start();

        // game timer
        gameLoop = new Timer(1000/60,this);
        gameLoop.start();
    }

    // Creating pipes
    public void placePipes() {
        // (0-256)
        // 128
        // 0 - 128 - (0-256) - pipeH/4 -> 3/4 pipeHeight
        int randomPipeY = (int)(pipeY - pipeH /4 -Math.random()*(pipeH/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeH + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        // background
        g.drawImage(backgroundImg, 0, 0, boardWidth,boardHeight, null);
        g.drawImage(bird.img, bird.x,bird.y,bird.width,bird.height,null);

        // drawing pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }
        // Score
        g.setFont((new Font("Arial", Font.PLAIN,32)));
        if(gameOver){
            g.drawString("Game Over:"+ (int) score,10,35);
        } else {
            g.drawString(String.valueOf((int)score),10,35);
        }
    }

    public  void move(){
        // bird
        velocityY += gravity;
       bird.y += velocityY;
       bird.y = Math.max(bird.y,0);

       // pipes
        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            // Tracking Score
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; // for 2 pipes 1: point
            }

            // checking Collision
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // GaveOver logic
        if(bird.y> boardHeight){
            gameOver = true;
        }
    }

    public boolean collision(Bird a , Pipe b){
        return a.x < b.x + b.width &&   //a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x &&   //a's top right corner passes b's top left corner
                a.y < b.y + b.height &&  //a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y;    //a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();

        if(gameOver){
            placePipesTimer.stop();
            gameLoop.stop();
        }

    }
    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE){
            velocityY = -9;

            if(gameOver){
               // Re-start Game
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


