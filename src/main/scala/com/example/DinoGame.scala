import java.awt._
import java.awt.event._
import javax.swing._
import scala.util.Random
import javax.imageio.ImageIO

object DinoGame extends App {
  SwingUtilities.invokeLater(() => {
    val frame = new JFrame("Dino Game")
    val panel = new GamePanel()

    frame.add(panel)
    frame.setSize(800, 400)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setVisible(true)
    panel.startGame()
  })
}

class GamePanel extends JPanel with ActionListener with KeyListener {
  private var timer: Timer = _
  private var dinoY: Int = 350 - 52
  private var velocityY: Int = 0
  private val gravity: Int = 1
  private var score: Int = 0
  private var coinsCollected: Int = 0
  private var speed: Int = 5
  private val random = new Random()

  private var obstacles: Array[(Int, Int)] = Array.fill(2)((800, 300)) // Start with 2 main obstacles
  private var coins: Array[(Int, Int)] = Array.fill(3)((800, random.nextInt(250)))
  private val dinoWidth = 65
  private val dinoHeight = 65
  private val obstacleWidth = 50
  private val obstacleHeight = 50
  private val coinWidth = 20
  private val coinHeight = 20
  private var paused: Boolean = false
  private var gameOver: Boolean = false

  private var jumpCount: Int = 3 // Jump count initialized to 3

  // Load images
  private var backgroundImage: Image = _
  private var coinImage: Image = _
  private var dinoImage: Image = _
  private var obstacleImage1: Image = _
  private var obstacleImage2: Image = _

  try {
    backgroundImage = ImageIO.read(getClass.getResource("/background.jpg"))
    coinImage = new ImageIcon(getClass.getResource("/coin_image.gif")).getImage
    dinoImage = ImageIO.read(getClass.getResource("/emojisky.com-226494.png"))
    obstacleImage1 = ImageIO.read(getClass.getResource("/cart.png")) // Obstacle 1 image
    obstacleImage2 = ImageIO.read(getClass.getResource("/obstacle2.png")) // Obstacle 2 image
  } catch {
    case e: Exception => println("Error loading resources: " + e.getMessage)
  }

  def startGame(): Unit = {
    timer = new Timer(20, this) // 50 FPS
    timer.start()
    addKeyListener(this)
    setFocusable(true)
    requestFocus()
  }

  override def actionPerformed(e: ActionEvent): Unit = {
    if (paused) return

    // Dino jump mechanics
    if (dinoY < 350 - dinoHeight) velocityY += gravity
    dinoY += velocityY
    if (dinoY >= 350 - dinoHeight) {
      dinoY = 350 - dinoHeight
      velocityY = 0
      jumpCount = 3 // Reset jump count when on the ground
    }

    // Obstacle movement
    for (i <- obstacles.indices) {
      val (x, y) = obstacles(i)
      val newX = x - speed
      obstacles(i) = if (newX < 0) (800 + random.nextInt(200), y) else (newX, y)

      if (new Rectangle(centerX - dinoWidth / 2, dinoY, dinoWidth, dinoHeight).intersects(
          new Rectangle(obstacles(i)._1, obstacles(i)._2, obstacleWidth, obstacleHeight)
        )) {
        gameOver = true
        timer.stop()
        JOptionPane.showMessageDialog(this,
          s"Game Over! Your score: $score\nCoins Collected: $coinsCollected\nPress R to Restart.",
          "Game Over",
          JOptionPane.INFORMATION_MESSAGE)
      }
    }

    // Coin movement and collection
    for (i <- coins.indices) {
      val (x, y) = coins(i)
      val newX = x - speed
      coins(i) = if (newX < 0) (800 + random.nextInt(200), random.nextInt(250)) else (newX, y)

      if (new Rectangle(centerX - dinoWidth / 2, dinoY, dinoWidth, dinoHeight).intersects(
          new Rectangle(coins(i)._1, coins(i)._2, coinWidth, coinHeight)
        )) {
        coins(i) = (800 + random.nextInt(200), random.nextInt(250))
        coinsCollected += 1
      }
    }

    score += 1
    if (score % 100 == 0) speed += 1

    repaint()
  }

  private def centerX: Int = getWidth / 2

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val g2d = g.asInstanceOf[Graphics2D]

    // Draw background
    if (backgroundImage != null) {
      g2d.drawImage(backgroundImage, 0, 0, getWidth, getHeight, this)
    }

    g2d.setColor(Color.BLACK)
    g2d.fillRect(0, 350, getWidth, 50)

    // Draw Dino
    if (!gameOver) {
      if (dinoImage != null) {
        g2d.drawImage(dinoImage, centerX - dinoWidth / 2, dinoY, dinoWidth, dinoHeight, this)
      } else {
        g2d.setColor(Color.RED)
        g2d.fillRect(centerX - dinoWidth / 2, dinoY, dinoWidth, dinoHeight)
      }
    }

    // Obstacles
    for (i <- obstacles.indices) {
      val (x, y) = obstacles(i)
      if (i == 0 && obstacleImage1 != null) { // Draw obstacle 1
        g2d.drawImage(obstacleImage1, x, y, obstacleWidth, obstacleHeight, this)
      } else if (i == 1 && obstacleImage2 != null) { // Draw obstacle 2
        g2d.drawImage(obstacleImage2, x, y, obstacleWidth, obstacleHeight, this)
      } else { // Fallback to colored rectangle for additional obstacles
        g2d.setColor(Color.BLUE)
        g2d.fillRect(x, y, obstacleWidth, obstacleHeight)
      }
    }

    // Coins
    if (coinImage != null) {
      for ((x, y) <- coins) {
        g2d.drawImage(coinImage, x, y, coinWidth, coinHeight, this)
      }
    } else {
      g2d.setColor(Color.YELLOW)
      for ((x, y) <- coins) {
        g2d.fillOval(x, y, coinWidth, coinHeight)
      }
    }

    // Score and coins display
    g2d.setColor(Color.BLACK)
    g2d.setFont(new Font("Arial", Font.BOLD, 20))
    g2d.drawString(s"Score: $score", 10, 20)
    g2d.drawString(s"Coins: $coinsCollected", 10, 40)

    if (paused) {
      g2d.setColor(Color.RED)
      g2d.drawString("Game Paused! Press P to Resume.", getWidth / 2 - 100, getHeight / 2)
    }

    if (gameOver) {
      g2d.setColor(Color.YELLOW)
      g2d.setFont(new Font("Arial", Font.BOLD, 30))
      g2d.drawString("GAME OVER", getWidth / 2 - 100, getHeight / 2 - 40)
      g2d.drawString(s"Your score: $score", getWidth / 2 - 100, getHeight / 2)
      g2d.drawString(s"Coins Collected: $coinsCollected", getWidth / 2 - 100, getHeight / 2 + 30)
      g2d.drawString("Press 'R' to Restart", getWidth / 2 - 100, getHeight / 2 + 60)
    }
  }

  override def keyPressed(e: KeyEvent): Unit = {
    if (e.getKeyCode == KeyEvent.VK_SPACE && jumpCount > 0) {
      velocityY = -15
      jumpCount -= 1 // Decrease jump count on every jump
    }

    if (e.getKeyCode == KeyEvent.VK_P) {
      paused = !paused
      if (!paused) startGame()
    }

    if (gameOver && e.getKeyCode == KeyEvent.VK_R) {
      resetGame()
    }
  }

  override def keyReleased(e: KeyEvent): Unit = {}

  override def keyTyped(e: KeyEvent): Unit = {}

  private def resetGame(): Unit = {
    dinoY = 350 - dinoHeight
    velocityY = 0
    score = 0
    speed = 5
    obstacles = Array.fill(2)((800, 300)) // Reset to 2 main obstacles
    coins = Array.fill(3)((800, random.nextInt(250)))
    jumpCount = 3 // Reset jump count
    gameOver = false
    paused = false
    startGame()
  }
}
