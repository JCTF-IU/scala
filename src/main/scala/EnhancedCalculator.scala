import java.awt.*
import java.awt.event.{ActionEvent, ActionListener}
import java.io.{File, IOException}
import java.net.URI
import javax.swing.*
import javax.swing.WindowConstants.EXIT_ON_CLOSE
import scala.Option

object CalculatorDemo extends App {
  val win = new CalculatorFrame
}

class CalculatorFrame extends JFrame with ActionListener {
  private val sBuilder = new StringBuilder
  private var firstOperand: Double = 0.0
  private var operation: Option[Operation] = None
  private var equalsCount: Int = 0
  private var cButtonCount: Int = 0
  private var ceButtonCount: Int = 0

   private val textArea = new JTextArea(1, 20)
  textArea.setEditable(false)

   private val buttons = Array(
    "C", "CE", "%", "÷", "7", "8", "9", "x", "4", "5", "6", "-", "1", "2", "3", "+", "e", "0", ".", "="
  )

   private def createButtonPanel: JPanel = {
    val panel = new JPanel(new GridLayout(5, 4))
    buttons.foreach { btnText =>
      val btn = new JButton(btnText)
      btn.addActionListener(this)
      panel.add(btn)
    }
    panel
  }

    private def createDisplayPanel: JPanel = {
    val panel = new JPanel(new BorderLayout)
    panel.add(new JScrollPane(textArea), BorderLayout.CENTER)
    panel
  }

  def actionPerformed(e: ActionEvent): Unit = {
    val btnText = e.getActionCommand
    btnText match {
      case "C" =>
      cButtonCount += 1
      if (cButtonCount == 2) {
        // 连续按两次"C"，创建文本文件
        createTextFile()
        // 重置"C"按钮计数器
        cButtonCount = 0
      } else {
        // 如果只按了一次，则执行常规的清除操作
        reset()
      }
      case "CE" =>
        ceButtonCount += 1
        if (ceButtonCount == 2) {
          // 连续点击两次CE，运行Rain.jar
          runJarFile("Rain.jar")
          // 重置CE按钮计数器
          ceButtonCount = 0
        } else {
          // 只点击一次CE，清除操作
          sBuilder.setLength(0)
          updateDisplay()
        }
      case digit if digit.matches("\\d|\\.") => sBuilder.append(digit); updateDisplay()
      case "+" | "-" | "x" | "÷" | "%" | "e" => setOperation(Operation.fromString(btnText))
      case "=" =>
        equalsCount += 1
        if (equalsCount == 2) {
          // 弹出确认对话框，询问是否打开网页
          val result = JOptionPane.showConfirmDialog(this, "您确定要打开网页吗？", "确认", JOptionPane.YES_NO_OPTION)
          if (result == JOptionPane.YES_OPTION) {
            // 打开浏览器
            if (!Desktop.isDesktopSupported()) {
              System.err.println("Desktop is not supported")
              return
            }
            val desktop = Desktop.getDesktop
            try {
              // 修改为您想要打开的网址
              val uri = new URI("https://ys.mihoyo.com/cloud/#/")
              desktop.browse(uri)
            } catch {
              case e: Exception => e.printStackTrace()
            }
          }
          equalsCount = 0 // 重置等号计数器
        } else {
          // 假设这是处理其他按键或单次等号按下的方法
          evaluate()
        }
      case _ => // Ignore other buttons
    }
  }

  private def createTextFile(): Unit = {
    val fileName = "file.txt"
    try {
      val file = new File(fileName)
      if (!file.exists()) {
        file.createNewFile()
        JOptionPane.showMessageDialog(this, s"文本文件已创建: $fileName")
      } else {
        JOptionPane.showMessageDialog(this, "文件已存在！")
      }
    } catch {
      case e: IOException =>
        JOptionPane.showMessageDialog(this, "创建文件时出错: " + e.getMessage())
    }
  }

  private def runJarFile(jarFileName: String): Unit = {
  val jarFile = new File(jarFileName)
  if (jarFile.exists()) {
    try {
      val cmd = Array("java", "-jar", jarFileName)
      Runtime.getRuntime.exec(cmd)
    } catch {
      case e: Exception =>
        JOptionPane.showMessageDialog(this, s"无法运行 JAR 文件: ${e.getMessage}")
    }
  } else {
    JOptionPane.showMessageDialog(this, s"JAR 文件不存在: $jarFileName")
  }
}

  private def setOperation(op: Option[Operation]): Unit = {
    operation = op
    op.foreach { _ =>
      firstOperand = sBuilder.toString.toDouble
      sBuilder.setLength(0)
      updateDisplay()
    }
  }

  def evaluate(): Unit = {
    operation.foreach { op =>
      val secondOperand = sBuilder.toString.toDouble
      val result = op match {
        case Addition => firstOperand + secondOperand
        case Subtraction => firstOperand - secondOperand
        case Multiplication => firstOperand * secondOperand
        case Division => if (secondOperand != 0) firstOperand / secondOperand else Double.NaN
        case Modulus => firstOperand % secondOperand
        case Exponentiation => math.pow(firstOperand, secondOperand)
      }
      sBuilder.setLength(0)
      sBuilder.append(result)
      updateDisplay()
      resetOperation()
    }
  }

  private def resetOperation(): Unit = operation = None

      private def reset(): Unit = {
    sBuilder.setLength(0)
    updateDisplay()
    resetOperation()
  }

  private def updateDisplay(): Unit = textArea.setText(sBuilder.toString)

  initUI()

  private def initUI(): Unit = {
    setTitle("计算器")
    setBounds(100,100,500,500)
    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE)

    val displayPanel = createDisplayPanel
    val buttonPanel = createButtonPanel
    buttonPanel.getComponent(19).setBackground(Color.ORANGE) // Set '=' button background to orange

    getContentPane.add(displayPanel, BorderLayout.NORTH)
    getContentPane.add(buttonPanel, BorderLayout.CENTER)

    pack()
    setVisible(true)
  }
}

sealed trait Operation
case object Addition extends Operation
case object Subtraction extends Operation
case object Multiplication extends Operation
case object Division extends Operation
case object Modulus extends Operation
case object Exponentiation extends Operation

object Operation {
  def fromString(op: String): Option[Operation] = op match {
    case "+" => Some(Addition)
    case "-" => Some(Subtraction)
    case "x" | "*" => Some(Multiplication)
    case "÷" => Some(Division)
    case "%" => Some(Modulus)
    case "e" => Some(Exponentiation)
    case _ => None
  }
}