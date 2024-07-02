import java.awt.*
import java.awt.event.{ActionEvent, ActionListener, KeyAdapter, KeyEvent}
import javax.swing.*
import scala.util.Random

object Rain extends JDialog with ActionListener {  
  
  private val random = new Random()  
  private val screenSize = Toolkit.getDefaultToolkit.getScreenSize  
  private val graphicsPanel = new GraphicsPanel()  
  private val gap = 20  
  private val posArr = Array.ofDim[Int](screenSize.width / gap + 1)  
  private val lines = screenSize.height / gap  
  private val columns = screenSize.width / gap  
  
  def main(args: Array[String]): Unit = {  
    SwingUtilities.invokeLater(() => {  
      initComponents()  
      setVisible(true)  
    })  
  }  
  
  private def initComponents(): Unit = {  
    setLayout(new BorderLayout())  
    add(graphicsPanel, BorderLayout.CENTER)  
  
    val toolkit = Toolkit.getDefaultToolkit  
    val image = toolkit.createImage(new java.awt.image.MemoryImageSource(0, 0, null, 0, 0))  
    val invisibleCursor = toolkit.createCustomCursor(image, new Point(0, 0), "cursor")  
    setCursor(invisibleCursor)  
  
    addKeyListener(new KeyPressListener())  
  
    setUndecorated(true)  
    getGraphicsConfiguration.getDevice.setFullScreenWindow(this)  
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)  
  
    for (i <- posArr.indices) {  
      posArr(i) = random.nextInt(lines)  
    }  
  
    new Timer(100, this).start()  
  }  
  
  def getChr(): Char = {  
    (random.nextInt(94) + 33).toChar  
  }  
  
  override def actionPerformed(e: ActionEvent): Unit = {  
    graphicsPanel.repaint()  
  }  
  
  private class GraphicsPanel extends JPanel {  
    override def paint(g: Graphics): Unit = {  
      val g2d = g.asInstanceOf[Graphics2D]  
      g2d.setFont(getFont.deriveFont(Font.BOLD))  
      g2d.setColor(Color.BLACK)  
      g2d.fillRect(0, 0, screenSize.width, screenSize.height)  
  
      var currentColumn = 0  
      for (x <- 0 until screenSize.width by gap) {  
        val endPos = posArr(currentColumn)  
        g2d.setColor(Color.CYAN)  
        g2d.drawString(getChr().toString, x, endPos * gap)  
  
        var cg = 0  
        for (j <- endPos - 15 until endPos) {  
          cg += 20  
          if (cg > 255) cg = 255  
          g2d.setColor(new Color(0, cg, 0))  
          g2d.drawString(getChr().toString, x, j * gap)  
        }  
  
        posArr(currentColumn) += random.nextInt(5)  
        if (posArr(currentColumn) * gap > getHeight) {  
          posArr(currentColumn) = random.nextInt(lines)  
        }  
        currentColumn += 1  
      }  
    }  
  }  
  
  private class KeyPressListener extends KeyAdapter {  
    override def keyPressed(e: KeyEvent): Unit = {  
      if (e.getKeyCode == KeyEvent.VK_ESCAPE) {  
        System.exit(0)  
      }  
    }  
  }  
}