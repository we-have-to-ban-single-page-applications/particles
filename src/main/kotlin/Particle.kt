import java.awt.Color
import java.awt.geom.Point2D

class Particle(
    val location: Point2D.Double,
    val color: Color,
    val velocity: Point2D.Double = Point2D.Double(),
) {
    companion object {
        const val SIZE = 5.0
    }
}
