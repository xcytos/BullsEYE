import java.awt._
import java.awt.event._
import javax.swing._
import scala.util.Random
import javax.imageio.ImageIO
import java.io.File
import java.awt.Desktop
import java.io.{FileWriter, PrintWriter, BufferedReader, FileReader}
import scala.collection.mutable.Map

object BullseyeGame extends App {
  // Create resources directory if it doesn't exist
  val resourcesDir = new File("src/main/resources")
  if (!resourcesDir.exists()) {
    resourcesDir.mkdirs()
  }

  // Create users.txt in resources directory
  val usersFile = new File("src/main/resources/users.txt")
  if (!usersFile.exists()) {
    usersFile.createNewFile()
  }

  SwingUtilities.invokeLater(() => {
    val loginDialog = new UserLoginDialog(null)
    if (loginDialog.userId != null) {
      val frame = new JFrame("Bullseye Game")
      val panel = new GamePanel(loginDialog.userId, loginDialog.userCoins)
      frame.add(panel)
      frame.setSize(800, 400)
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      frame.setVisible(true)
      panel.startGame()
    }
  })
}

class UserLoginDialog(parent: JFrame) extends JDialog(parent, "User Login", true) {
  private val panel = new JPanel(new GridLayout(0, 1))
  private val idField = new JTextField(10)
  private val loginButton = new JButton("Login")
  var userId: String = null
  var userCoins: Int = 0

  private val usersFile = new File("src/main/resources/users.txt")

  panel.add(new JLabel("Enter User ID:"))
  panel.add(idField)
  panel.add(loginButton)

  loginButton.addActionListener(_ => {
    val id = idField.getText.trim
    if (id.nonEmpty) {
      userId = id
      userCoins = getUserCoins(id)
      dispose()
    } else {
      JOptionPane.showMessageDialog(this, "Please enter a valid ID")
    }
  })

  private def getUserCoins(id: String): Int = {
    val source = scala.io.Source.fromFile("src/main/resources/users.txt")
    try {
      source.getLines()
        .map(_.split(","))
        .find(_(0) == id)
        .map(_(1).toInt)
        .getOrElse(0)
    } finally {
      source.close()
    }
  }

  getContentPane.add(panel)
  pack()
  setLocationRelativeTo(parent)
  setVisible(true)
}

class GamePanel(userId: String, initialCoins: Int) extends JPanel with ActionListener with KeyListener {
  private var timer: Timer = _
  private var bullseyeY: Int = 350 - 52
  private val bullseyeX = 150
  private val MAX_JUMPS = 3
  private var jumpsRemaining = MAX_JUMPS
  private var gravity = 1
  private var velocityY = 0
  private var score = 0
  private var coinsCollected = initialCoins
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
  private var isOnGround = true

  private var backgroundImage: Image = _
  private var coinImage: Image = _
  private var bullseyeImage: Image = _
  private var obstacleImage1: Image = _
  private var obstacleImage2: Image = _

  private var highScore = 0

  loadResources()

  private def saveUserCoins(): Unit = {
    val tempFile = new File("src/main/resources/users_temp.txt")
    val reader = new BufferedReader(new FileReader("src/main/resources/users.txt"))
    val writer = new PrintWriter(new FileWriter(tempFile))
    
    var updated = false
    var line = reader.readLine()
    
    while (line != null) {
      val parts = line.split(",")
      if (parts(0) == userId) {
        writer.println(s"$userId,$coinsCollected")
        updated = true
      } else {
        writer.println(line)
      }
      line = reader.readLine()
    }
    
    if (!updated) {
      writer.println(s"$userId,$coinsCollected")
    }
    
    reader.close()
    writer.close()
    
    val usersFile = new File("src/main/resources/users.txt")
    usersFile.delete()
    tempFile.renameTo(usersFile)
  }

  private def loadResources(): Unit = {
    try {
      // Load images from the src/main/resources directory
      val resourcePath = "src/main/resources"
      coinImage = new ImageIcon(s"$resourcePath/coin_image.gif").getImage
      bullseyeImage = ImageIO.read(new File(s"$resourcePath/emojisky.com-226494.png"))
      obstacleImage1 = ImageIO.read(new File(s"$resourcePath/cart.png"))
      obstacleImage2 = ImageIO.read(new File(s"$resourcePath/obstacle2.png"))
      backgroundImage = ImageIO.read(new File(s"$resourcePath/bg3.jpg"))
    } catch {
      case e: Exception => 
        println("Error loading resources: " + e.getMessage)
        e.printStackTrace()
    }
  }

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
      velocityY += gravity
      bullseyeY += velocityY

      if (bullseyeY >= 350 - bullseyeHeight) {
        bullseyeY = 350 - bullseyeHeight
        velocityY = 0
        isOnGround = true
        jumpsRemaining = MAX_JUMPS
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
        saveUserCoins()
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
        saveUserCoins()
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

    if (score > highScore) {
      highScore = score
    }
  }

  private def resetRound(): Unit = {
    jumpsRemaining = MAX_JUMPS
    bullseyeY = 350 - bullseyeHeight
    velocityY = 0
    obstacles = Array.fill(2)((800, 300))
    coins = Array.fill(3)((800, random.nextInt(250)))
    score = 0
    speed = 5
    gameOver = false
    paused = false
    isOnGround = true
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
    g2d.drawString(s"User ID: $userId", 10, 20)
    g2d.drawString(s"High Score: $highScore", 10, 40)
    g2d.drawString(s"Score: $score", 10, 60)
    g2d.drawString(s"Coins: $coinsCollected", 10, 80)
    g2d.drawString(s"Jumps Left: $jumpsRemaining", 10, 100)
  }

  override def keyPressed(e: KeyEvent): Unit = {
    if (e.getKeyCode == KeyEvent.VK_SPACE && jumpsRemaining > 0) {
      velocityY = -15
      jumpsRemaining -= 1
      isOnGround = false
    }

    if (e.getKeyCode == KeyEvent.VK_P) {
      paused = !paused
    }

    if (e.getKeyCode == KeyEvent.VK_R && gameOver) {
      resetRound()
      timer.start()
    }

    if (e.getKeyCode == KeyEvent.VK_T && gameOver) {
      try {
        val file = new File("src/main/resources/target.html")
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
