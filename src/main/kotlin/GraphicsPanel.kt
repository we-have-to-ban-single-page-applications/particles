import java.awt.Canvas
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.random.Random

class GraphicsPanel :
    Canvas(),
    Runnable {
    private val bounds
        get() =
            Rectangle2D.Double(
                0.0,
                0.0,
                width.toDouble() - Particle.SIZE,
                height.toDouble() - Particle.SIZE,
            )

    private val rules =
        mapOf(
            Color.BLUE to Color.BLUE to -0.32,
            Color.BLUE to Color.GRAY to -0.17,
            Color.BLUE to Color.YELLOW to 0.34,
            Color.GRAY to Color.GRAY to -0.10,
            Color.GRAY to Color.BLUE to -0.34,
            Color.YELLOW to Color.YELLOW to 0.15,
            Color.YELLOW to Color.BLUE to -0.20,
        )

    override fun run() {
        val particles =
            bounds
                .let {
                    Stream.of(
                        generateParticles(200, Color.YELLOW, it),
                        generateParticles(200, Color.GRAY, it),
                        generateParticles(200, Color.BLUE, it),
                    )
                }.flatMap { it }
                .toList()

        Thread.startVirtualThread {
            val frameTime = 1_000 / 60
            while (true) {
                val start = System.currentTimeMillis()
                calculateParticles(particles)
                calculateBounds(particles)
                val elapsed = System.currentTimeMillis() - start
                val sleep = frameTime - elapsed
                if (sleep > 0) Thread.sleep(sleep)
            }
        }

        Thread.startVirtualThread {
            createBufferStrategy(2)
            val bs = bufferStrategy

            var frames = 0
            var fps = 0
            var lastTime = System.nanoTime()

            while (true) {
                val g = bs.drawGraphics as Graphics2D

                paintBackground(g)
                paintParticles(particles, g)
                paintFps(g, fps)

                g.dispose()
                bs.show()

                frames++
                val now = System.nanoTime()
                if (now - lastTime >= 1_000_000_000) {
                    fps = frames
                    frames = 0
                    lastTime = now
                }
            }
        }
    }

    private fun paintBackground(g: Graphics2D) {
        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)
    }

    private fun paintParticles(
        particles: List<Particle>,
        g: Graphics2D,
    ) {
        particles.parallelStream().forEach {
            g.color = it.color
            g.fill(Rectangle2D.Double(it.location.x, it.location.y, Particle.SIZE, Particle.SIZE))
        }
    }

    private fun paintFps(
        g: Graphics2D,
        fps: Int,
    ) {
        val text = fps.toString()
        val bounds = g.font.createGlyphVector(g.fontRenderContext, text).visualBounds

        g.color = Color.WHITE
        g.drawString(text, 0F, bounds.height.toFloat())
    }

    private fun generateParticles(
        count: Int,
        color: Color,
        bounds: Rectangle2D.Double,
    ) = Stream
        .generate {
            Particle(
                Point2D.Double(
                    Random.nextDouble(bounds.x, bounds.width),
                    Random.nextDouble(bounds.y, bounds.height),
                ),
                color,
            )
        }.limit(count.toLong())

    private fun calculateParticles(particles: List<Particle>) {
        val grouped = particles.groupBy { it.color }

        rules.forEach { (pair, g) ->
            val first = grouped[pair.first]
            val second = grouped[pair.second]

            if (first == null || second == null) return@forEach

            first.parallelStream().forEach { a ->
                var fx = 0.0
                var fy = 0.0

                second.forEach { b ->
                    if (a == b) return@forEach

                    val dx = a.location.x - b.location.x
                    val dy = a.location.y - b.location.y
                    val d = a.location.distance(b.location)

                    if (d > 0 && d < 80) {
                        val f = g / d
                        fx += f * dx
                        fy += f * dy
                    }
                }

                a.velocity.x = (a.velocity.x + fx) / 2
                a.velocity.y = (a.velocity.y + fy) / 2
                a.location.x += a.velocity.x
                a.location.y += a.velocity.y
            }
        }
    }

    private fun calculateBounds(particles: List<Particle>) {
        bounds.let {
            particles.parallelStream().forEach { particle ->
                when {
                    particle.location.x <= it.x ->
                        particle.velocity.x = abs(particle.velocity.x) + Particle.SIZE
                    particle.location.x >= it.width ->
                        particle.velocity.x = -abs(particle.velocity.x) - Particle.SIZE
                }
                when {
                    particle.location.y <= it.y ->
                        particle.velocity.y = abs(particle.velocity.y) + Particle.SIZE
                    particle.location.y >= it.height ->
                        particle.velocity.y = -abs(particle.velocity.y) - Particle.SIZE
                }
            }
        }
    }
}
