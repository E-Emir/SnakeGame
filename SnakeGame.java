import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList; //used for storing segments of the snake's body
import java.util.Random; //used for getting random x and y values to place food on screen 
import javax.swing.*;
import java.util.List;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    int boardWidth;
    int boardHeight;
    int tileSize = 25;

    // Snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    // Food
    Tile food;
    Random random;

    // Game logic
    Timer gameLoop;
    int velocityX;
    int velocityY;
    boolean gameOver = false;

    // Difficulty control
    int baseSpeed = 100; //Initial delay in ms
    int difficultyStep = 5; //Number of food items required to increase speed
    int foodConsumed = 0; //Tracks food consumption for difficulty adjustment


    private List<Player> leaderboard; //leaderboard List
    private String currentUsername; //Current player username


    // Constructor
    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true); //Emphasizes that SnakeGame is the one listening for key presses

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();

        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        velocityX = 0;
        velocityY = 0;

        gameLoop = new Timer(baseSpeed, this); //Starts at base speed
        gameLoop.start();

        // Leaderboard initialization
        leaderboard = new ArrayList<>();
        currentUsername = JOptionPane.showInputDialog(null, "Enter your username:");
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "Guest";
        }

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }


    //Method to draw the game and show score
    public void draw(Graphics g) {

        //Grid
        //for (int i = 0; i < boardWidth/tileSize; i++){
            // (x1, y1, x2, y2)
            //g.drawLine(i*tileSize, 0, i*tileSize, boardHeight); //vertical lines
            //g.drawLine(0, i*tileSize, boardWidth, i*tileSize); // horizontal lines
        //}

        // Food
        g.setColor(Color.red);
        g.fill3DRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, true);

        // Snake Head
        g.setColor(Color.green);
        g.fill3DRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, true);

        // Snake Body
        for (Tile snakePart : snakeBody) {
            g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
        }

        // Score tracker
        //Set the font of the text
        g.setFont(new Font("Serif", Font.PLAIN, 16));
        //Set the color of the text to white
        g.setColor(Color.white);
        //Display the current score as the length of the snake (number of body parts/food consumed)
        g.drawString("Score: " + snakeBody.size(), tileSize - 16, tileSize);

        // Game Over screen
        if (gameOver) {
            g.setFont(new Font("Serif", Font.BOLD, 36));
            FontMetrics metrics = g.getFontMetrics();
            String gameOverText = "GAME OVER";
            int gameOverX = (boardWidth - metrics.stringWidth(gameOverText)) / 2;
            int gameOverY = boardHeight / 2 - metrics.getHeight();
            
            g.setColor(Color.red);
            g.drawString(gameOverText, gameOverX, gameOverY);

           // Score display below "GAME OVER"
           g.setFont(new Font("Serif", Font.PLAIN, 24));
           //g.setColor(Color.white);
           String scoreText = "Final Score: " + snakeBody.size();
           int scoreX = (boardWidth - metrics.stringWidth(scoreText)) / 2;
           int scoreY = gameOverY + metrics.getHeight() + 20;
           g.drawString(scoreText, scoreX, scoreY); 
        }
        /* 
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameOver) {
            g.setColor(Color.RED);
            g.drawString("Game Over: " + snakeBody.size(), tileSize - 16, tileSize);
        } else {
            g.drawString("Score: " + snakeBody.size(), tileSize - 16, tileSize);
        }
        */

        if (isPaused) {
            g.setFont(new Font("Serif", Font.BOLD, 36));
            FontMetrics metrics = g.getFontMetrics();
            String pauseText = "Paused";
            int pauseX = (boardWidth - metrics.stringWidth(pauseText)) / 2;
            int pauseY = boardHeight / 2;
            g.setColor(Color.yellow);
            g.drawString(pauseText, pauseX, pauseY);
        }
    }

    //Method to place food at random coordiantes on the grid space
    public void placeFood() {
        //Randomly spawns food in different locations within the grid space
        food.x = random.nextInt(boardWidth / tileSize); //Makes sure food stays within grid width
        food.y = random.nextInt(boardHeight / tileSize); //Makes sure food stays within grid height
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    //Method for snake movement
    public void move() {
        // Eat food
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            foodConsumed++; 
            placeFood(); // spawn food once one is consumed
            adjustDifficulty(); // Adjust speed after eating food
        }

        // Snake Body, each body part follows the position of the part in front of it
        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) { // the snake's head is the first element in the body list
                snakePart.x = snakeHead.x; // snake's head moves to new position
                snakePart.y = snakeHead.y;
            } else { // for each body part
                Tile prevSnakePart = snakeBody.get(i - 1); // the part in front of it
                snakePart.x = prevSnakePart.x; // follows the previous segment
                snakePart.y = prevSnakePart.y;
            }
        }

        // Snake Head, moves snake head based on its velocity
        snakeHead.x += velocityX; // add the horizontal velocity to the head's X position
        snakeHead.y += velocityY; // add the vertical velocity to the head's Y position

        // Game over conditions
        for (Tile snakePart : snakeBody) {
            // checks if the snake's head collides with any part of its body
            if (collision(snakeHead, snakePart)) {
                gameOver = true; // end game if snake collides with itself
            }
        }

        // checks if the snakes goes out of the grid (by hitting the wall)
        if (snakeHead.x * tileSize < 0 || snakeHead.x * tileSize >= boardWidth || // checks if head is on left or right side
            snakeHead.y * tileSize < 0 || snakeHead.y * tileSize >= boardHeight) { // checks if head is off the top or off the bottom
            gameOver = true; // ends game if snake collides with wall
        }
    }

    public void adjustDifficulty() {
        if (foodConsumed % difficultyStep == 0) {
            int newDelay = Math.max(50, baseSpeed - (foodConsumed / difficultyStep) * 10);
            gameLoop.setDelay(newDelay); // Decrease delay to increase speed
        }
    }


    public int getScore(){
        return snakeBody.size();
    }


    public class Player {
        String username;
        int score;
    
        Player(String username, int score) {
            this.username = username;
            this.score = score;
        }
    
        public int getScore() {
            return score;
        }
    
        public String getUsername() {
            return username;
        }
    }


    private void resetGame() {
        snakeHead = new Tile(5, 5); //Reset snake's head position
        snakeBody.clear();         //Clear the snake's body
        placeFood();               //Reposition the food
        velocityX = 0;             //Reset movement direction
        velocityY = 0;
        foodConsumed = 0;          //Reset score
        gameOver = false;          //Reset game over state
        gameLoop.start();          //Restart the game loop
        repaint();                 //Refresh the screen
    }
    


    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        } else { // if the game is over
            gameLoop.stop(); // stop the game
            updateLeaderboard(); // update the leaderboard with the current score
            displayLeaderboard(); // show leaderboard, with username and score alongside
    
            // Ask the user if they want to play again
            int option = JOptionPane.showConfirmDialog(
                null,
                "GAME OVER! Do you want to play again?", // message displayed in dialog
                "Play Again", // title of dialog window
                JOptionPane.YES_NO_OPTION // gives option to player to continue playing or not
            );
    
            if (option == JOptionPane.YES_OPTION) {
                resetGame(); //Reset the game
                String username = JOptionPane.showInputDialog(null, "Enter your username:"); // Ask for username
                if (username == null || username.isEmpty()) {
                    username = "Guest"; //Default to "Guest" if no username is entered
                }
                currentUsername = username; //Set the new username for the current game
            } else {
                System.exit(0); //Exit the game
            }
        }
    }
    
    private void updateLeaderboard() {
        leaderboard.add(new Player(currentUsername, getScore()));
        // Sort leaderboard by score in descending order
        leaderboard.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
    }
    
    private void displayLeaderboard() {
        StringBuilder leaderboardDisplay = new StringBuilder("Leaderboard:\n");
        int rank = 1;
        for (Player player : leaderboard) {
            leaderboardDisplay.append(rank)
            .append(". ")
            .append(player.getUsername())
            .append(" - ")
            .append(player.getScore())
            .append("\n");
            rank++;
        }
        JOptionPane.showMessageDialog(null, leaderboardDisplay.toString(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean isPaused = false;


    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P){ //User presses "P" to pause the game
            isPaused = !isPaused; //Toggle pause state
            if (isPaused){
                gameLoop.stop(); //Stops the game loop
            }
            else {
                gameLoop.start(); //Resume the game loop
            }
            repaint();
        }
        if (!isPaused){
            // When the UP arrow key is pressed, the snake moves up, and ensures it's not already moving down (otherwise the snake would collide with itself)
            if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
                velocityX = 0; // stops horizontal movement
                velocityY = -1; // Move the snake upward by decreasing the Y-coordinate 
            // When the DOWN arrow key is pressed, the snake moves down, and ensures it's not already moving up (otherwise the snake would collide with itself)
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
                velocityX = 0; // stops horizontal movement
                velocityY = 1; // Move the snake downward by increasing the Y-coordinate
            // When the LEFT arrow is pressed, the snake moves to the left, and ensures it's not already moving in the right direction (otherwise the snake would collide with itself)
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
                velocityX = -1; // Move the snake left by decreasing the X-coordinate
                velocityY = 0; // stops vertical movement
            // When the RIGHT arrow is pressed, the snake moves to the right, and ensures it's not already moving in the left direction (otherwise the snake would collide with itself)
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
                velocityX = 1; // Move the snake right by increasing the X-coordinate
                velocityY = 0; // stops vertical movement
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}




