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
  private val dinoWidth = 65
  private val dinoHeight = 65
  private val obstacleWidth = 50
  private val obstacleHeight = 50
  private val coinWidth = 20
  private val coinHeight = 20
  private var paused = false
  private var gameOver = false
  private var isOnGround = true // Track if the Dino is on the ground

  private var backgroundImage: Image = _
  private var coinImage: Image = _
  private var dinoImage: Image = _
  private var obstacleImage1: Image = _
  private var obstacleImage2: Image = _

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
      // Apply gravity when the Dino is in the air
      velocityY += gravity
      dinoY += velocityY

      // Check if the Dino hits the ground
      if (dinoY >= 350 - dinoHeight) {
        dinoY = 350 - dinoHeight // Keep Dino on the ground
        velocityY = 0 // Reset velocity
        isOnGround = true // Dino is on the ground
        jumpsRemaining = MAX_JUMPS // Reset jump count
      }
    }
  }

  private def handleCollisions(): Unit = {
    for (i <- obstacles.indices) {
      val (x, y) = obstacles(i)
      if (new Rectangle(centerX - dinoWidth / 2, dinoY, dinoWidth, dinoHeight).intersects(
        new Rectangle(x, y, obstacleWidth, obstacleHeight)
      )) {
        gameOver = true
        timer.stop()
      }
    }

    for (i <- coins.indices) {
      val (x, y) = coins(i)
      if (new Rectangle(centerX - dinoWidth / 2, dinoY, dinoWidth, dinoHeight).intersects(
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
  }

  private def resetRound(): Unit = {
    jumpsRemaining = MAX_JUMPS
    dinoY = 350 - dinoHeight // Ensure dino starts at the ground level
    velocityY = 0
    obstacles = Array.fill(2)((800, 300)) // Reset obstacles position
    coins = Array.fill(3)((800, random.nextInt(250))) // Reset coins position
    score = 0
    coinsCollected = 0
    speed = 5
    gameOver = false
    paused = false
    isOnGround = true // Reset Dino to be on the ground
  }

  private def centerX: Int = getWidth / 2

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val g2d = g.asInstanceOf[Graphics2D]

    if (backgroundImage != null) {
      g2d.drawImage(backgroundImage, 0, 0, getWidth, getHeight, this)
    }

    g2d.setColor(Color.BLACK)
    g2d.fillRect(0, 350, getWidth, 50)

    renderDino(g2d)
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
    }
  }

  private def renderDino(g2d: Graphics2D): Unit = {
    if (dinoImage != null) {
      g2d.drawImage(dinoImage, centerX - dinoWidth / 2, dinoY, dinoWidth, dinoHeight, this)
    } else {
      g2d.setColor(Color.RED)
      g2d.fillRect(centerX - dinoWidth / 2, dinoY, dinoWidth, dinoHeight)
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
    g2d.drawString(s"Score: $score", 10, 20)
    g2d.drawString(s"Coins: $coinsCollected", 10, 40)
    g2d.drawString(s"Jumps Left: $jumpsRemaining", 10, 60)
  }

  private def loadResources(): Unit = {
    try {
      backgroundImage = ImageIO.read(getClass.getResource("/background.jpg"))
      coinImage = new ImageIcon(getClass.getResource("/coin_image.gif")).getImage
      dinoImage = ImageIO.read(getClass.getResource("/emojisky.com-226494.png"))
      obstacleImage1 = ImageIO.read(getClass.getResource("/cart.png"))
      obstacleImage2 = ImageIO.read(getClass.getResource("/obstacle2.png"))
    } catch {
      case e: Exception => println("Error loading resources: " + e.getMessage)
    }
  }

  override def keyPressed(e: KeyEvent): Unit = {
    if (e.getKeyCode == KeyEvent.VK_SPACE && jumpsRemaining > 0) {
      velocityY = -15  // Set upward velocity for the jump
      jumpsRemaining -= 1 // Decrease jump count
      isOnGround = false // Dino is in the air now
    }

    if (e.getKeyCode == KeyEvent.VK_P) {
      paused = !paused
    }

    if (e.getKeyCode == KeyEvent.VK_R && gameOver) {
      resetRound() // Reset everything to start a new game
      timer.start()
    }
  }

  override def keyReleased(e: KeyEvent): Unit = {}

  override def keyTyped(e: KeyEvent): Unit = {}
}
