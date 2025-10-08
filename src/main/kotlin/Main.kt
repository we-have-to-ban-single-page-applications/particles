import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

fun main() {
    SwingUtilities.invokeLater {
        val graphicsPanel = GraphicsPanel()

        JFrame("Particles").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            setSize(800, 600)
            setLocationRelativeTo(null)
            contentPane.add(graphicsPanel)
            isVisible = true
        }

        graphicsPanel.run()
    }
}
