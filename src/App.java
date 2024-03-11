import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
       int boardWidth = 360;
       int boardHeight = 640;

       JFrame f = new JFrame("Flappy Bird");
       f.setVisible(true);
       f.setSize(boardWidth,boardHeight);
       f.setLocationRelativeTo(null);
       f.setResizable(false);
       f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       FlappyBird flappyBird = new FlappyBird();
       f.add(flappyBird);
       f.pack(); // we need mentioned width, height without bar
       flappyBird.requestFocus();
       f.setVisible(true);
    }
}
