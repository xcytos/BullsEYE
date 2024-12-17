import java.awt._
import java.awt.event._
import javax.swing._
import scala.util.Random
import javax.imageio.ImageIO
import java.io.File
import java.awt.Desktop

object BullseyeGame extends App {
  SwingUtilities.invokeLater(() => {
    val frame = new JFrame("Bullseye Game")
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
  private var bullseyeY: Int = 350 - 52
  private val bullseyeX = 150 // Fixed X position for Bullseye, closer to the left
  private val MAX_JUMPS = 3
  private var jumpsRemaining = MAX_JUMPS
  private var gravity = 1
  private var velocityY = 0
  private var score = 0
  private var coinsCollected = 0
  private var speed = 5
  private val random = new Random()
  private var obstacles: Array[(Int, Int)] = Array.fill(2)((800, 300))
  private var coins: Array[(Int, Int)] = Array.fill(3)((800, random.nextInt(250)))
  private val bullseyeWidth = 65
  private val bullseyeHeight = 65
  private val obstacleWidth = 50
  private val obstacleHeight = 50
  private val coinWidth = 40
  private val coinHeight = 40
  private var paused = false
  private var gameOver = false
  private var isOnGround = true // Track if Bullseye is on the ground

  private var backgroundImage: Image = _
  private var coinImage: Image = _
  private var bullseyeImage: Image = _
  private var obstacleImage1: Image = _
  private var obstacleImage2: Image = _

  // High score tracking
  private var highScore = 0

  // Load resources
  loadResources()

  def startGame(): Unit = {
    timer = new Timer(20, this)
    timer.start()
    addKeyListener(this)
    setFocusable(true)
    requestFocus()
    resetRound()
  }

  override def actionPerformed(e: ActionEvent): Unit = {
    if (paused || gameOver) return

    updatePhysics()
    handleCollisions()
    moveObjects()
    updateScore()

    repaint()
  }

  private def updatePhysics(): Unit = {
    if (!isOnGround) {
      // Apply gravity when Bullseye is in the air
      velocityY += gravity
      bullseyeY += velocityY

      // Check if Bullseye hits the ground
      if (bullseyeY >= 350 - bullseyeHeight) {
        bullseyeY = 350 - bullseyeHeight // Keep Bullseye on the ground
        velocityY = 0 // Reset velocity
        isOnGround = true // Bullseye is on the ground
        jumpsRemaining = MAX_JUMPS // Reset jump count
      }
    }
  }

  private def handleCollisions(): Unit = {
    for (i <- obstacles.indices) {
      val (x, y) = obstacles(i)
      if (new Rectangle(bullseyeX, bullseyeY, bullseyeWidth, bullseyeHeight).intersects(
        new Rectangle(x, y, obstacleWidth, obstacleHeight)
      )) {
        gameOver = true
        timer.stop()
      }
    }

    for (i <- coins.indices) {
      val (x, y) = coins(i)
      if (new Rectangle(bullseyeX, bullseyeY, bullseyeWidth, bullseyeHeight).intersects(
        new Rectangle(x, y, coinWidth, coinHeight)
      )) {
        coins(i) = (800 + random.nextInt(200), random.nextInt(250))
        coinsCollected += 1
      }
    }
  }

  private def moveObjects(): Unit = {
    for (i <- obstacles.indices) {
      val (x, y) = obstacles(i)
      val newX = x - speed
      obstacles(i) = if (newX < 0) (800 + random.nextInt(200), y) else (newX, y)
    }

    for (i <- coins.indices) {
      val (x, y) = coins(i)
      val newX = x - speed
      coins(i) = if (newX < 0) (800 + random.nextInt(200), random.nextInt(250)) else (newX, y)
    }
  }

  private def updateScore(): Unit = {
    score += 1
    if (score % 100 == 0) {
      speed += 1
    }

    // Update high score if current score is higher
    if (score > highScore) {
      highScore = score
    }
  }

  private def resetRound(): Unit = {
    jumpsRemaining = MAX_JUMPS
    bullseyeY = 350 - bullseyeHeight // Ensure Bullseye starts at the ground level
    velocityY = 0
    obstacles = Array.fill(2)((800, 300)) // Reset obstacles position
    coins = Array.fill(3)((800, random.nextInt(250))) // Reset coins position
    score = 0
    coinsCollected = 0
    speed = 5
    gameOver = false
    paused = false
    isOnGround = true // Reset Bullseye to be on the ground
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val g2d = g.asInstanceOf[Graphics2D]

    if (backgroundImage != null) {
      g2d.drawImage(backgroundImage, 0, 0, getWidth, getHeight, this)
    }

    g2d.setColor(Color.BLACK)
    g2d.fillRect(0, 350, getWidth, 50)

    renderBullseye(g2d)
    renderObstacles(g2d)
    renderCoins(g2d)

    renderHUD(g2d)

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
      g2d.drawString("Press 'T' to Redeem", getWidth / 2 - 100, getHeight / 2 + 90)
    }
  }

  private def renderBullseye(g2d: Graphics2D): Unit = {
    if (bullseyeImage != null) {
      g2d.drawImage(bullseyeImage, bullseyeX, bullseyeY, bullseyeWidth, bullseyeHeight, this)
    } else {
      g2d.setColor(Color.RED)
      g2d.fillRect(bullseyeX, bullseyeY, bullseyeWidth, bullseyeHeight)
    }
  }

  private def renderObstacles(g2d: Graphics2D): Unit = {
    for (i <- obstacles.indices) {
      val (x, y) = obstacles(i)
      val obstacleImage = if (i == 0) obstacleImage1 else obstacleImage2
      if (obstacleImage != null) {
        g2d.drawImage(obstacleImage, x, y, obstacleWidth, obstacleHeight, this)
      } else {
        g2d.setColor(Color.BLUE)
        g2d.fillRect(x, y, obstacleWidth, obstacleHeight)
      }
    }
  }

  private def renderCoins(g2d: Graphics2D): Unit = {
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
  }

  private def renderHUD(g2d: Graphics2D): Unit = {
    g2d.setColor(Color.BLACK)
    g2d.setFont(new Font("Arial", Font.BOLD, 20))
    g2d.drawString(s"High Score: $highScore", 10, 20) // Display high score above score
    g2d.drawString(s"Score: $score", 10, 40)
    g2d.drawString(s"Coins: $coinsCollected", 10, 60)
    g2d.drawString(s"Jumps Left: $jumpsRemaining", 10, 80)
  }

  private def loadResources(): Unit = {
    try {
      coinImage = new ImageIcon(getClass.getResource("/coin_image.gif")).getImage
      bullseyeImage = ImageIO.read(getClass.getResource("/emojisky.com-226494.png"))
      obstacleImage1 = ImageIO.read(getClass.getResource("/cart.png"))
      obstacleImage2 = ImageIO.read(getClass.getResource("/obstacle2.png"))
      backgroundImage = ImageIO.read(getClass.getResource("/bg3.jpg"))
    } catch {
      case e: Exception => println("Error loading resources: " + e.getMessage)
    }
  }

  override def keyPressed(e: KeyEvent): Unit = {
    if (e.getKeyCode == KeyEvent.VK_SPACE && jumpsRemaining > 0) {
      velocityY = -15  // Set upward velocity for the jump
      jumpsRemaining -= 1 // Decrease jump count
      isOnGround = false // Bullseye is in the air now
    }

    if (e.getKeyCode == KeyEvent.VK_P) {
      paused = !paused
    }

    if (e.getKeyCode == KeyEvent.VK_R && gameOver) {
      resetRound() // Reset everything to start a new game
      timer.start()
    }

    if (e.getKeyCode == KeyEvent.VK_T && gameOver) {
      // Redirect to a local HTML file after pressing 'T'
      try {
        val file = new File("src/main/resources/target.html") // Update with the correct path to your HTML file
        if (file.exists()) {
          if (Desktop.isDesktopSupported) {
            Desktop.getDesktop().browse(file.toURI)
          } else {
            println("Desktop is not supported on this system.")
          }
        } else {
          println("The specified HTML file does not exist.")
        }
      } catch {
        case e: Exception => println(s"Error opening the HTML file: ${e.getMessage}")
      }
    }
  }

  override def keyReleased(e: KeyEvent): Unit = {}

  override def keyTyped(e: KeyEvent): Unit = {}
}
