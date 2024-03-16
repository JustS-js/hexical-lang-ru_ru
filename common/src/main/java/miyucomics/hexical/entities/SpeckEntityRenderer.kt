package miyucomics.hexical.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.client.CAP_THETA
import at.petrak.hexcasting.client.findDupIndices
import at.petrak.hexcasting.client.makeZappy
import at.petrak.hexcasting.client.rotate
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.*
import kotlin.math.*

class SpeckEntityRenderer(ctx: EntityRendererFactory.Context?) : EntityRenderer<SpeckEntity?>(ctx) {
	override fun render(entity: SpeckEntity?, yaw: Float, tickDelta: Float, matrices: MatrixStack?, vertexConsumers: VertexConsumerProvider?, light: Int) {
		val oldShader = RenderSystem.getShader()
		RenderSystem.setShader(GameRenderer::getPositionColorShader)
		RenderSystem.enableDepthTest()
		matrices!!.push()

		val size = 0.25f
		val pattern = entity!!.getPattern()
		val lines = pattern.toLines(size, pattern.getCenter(size).negate()).toMutableList()
		for (i in lines.indices)
			lines[i] = Vec2f(lines[i].x, -lines[i].y)
		val zappy = makeZappy(lines, findDupIndices(pattern.positions()), 10, 1.5f, 0.1f, 0.2f, 0f, 1f, hashCode().toDouble())
		if (entity.yaw != 0.0f)
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-entity.yaw))
		if (entity.pitch != 0.0f)
			matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.pitch))
		drawPattern(matrices.peek().positionMatrix, zappy, 0.05f, entity.getPigment())
		matrices.pop()
		RenderSystem.setShader { oldShader }
	}

	override fun getTexture(entity: SpeckEntity?): Identifier? { return null }
}

fun drawPattern(mat: Matrix4f, points: List<Vec2f>, width: Float, colorizer: FrozenColorizer) {
	if (points.size <= 1) return
	val tess = Tessellator.getInstance()
	val buf = tess.buffer
	val n = points.size
	val joinAngles = FloatArray(n)
	val joinOffsets = FloatArray(n)
	for (i in 2 until n) {
		val p0 = points[i - 2];
		val p1 = points[i - 1];
		val p2 = points[i];
		val prev = p1.add(p0.negate())
		val next = p2.add(p1.negate())
		val angle = atan2((prev.x * next.y - prev.y * next.x).toDouble(), (prev.x * next.x + prev.y * next.y).toDouble()).toFloat()
		joinAngles[i - 1] = angle
		val clamp = min(prev.length(), next.length()) / (width * 0.5f);
		joinOffsets[i - 1] = MathHelper.clamp(sin(angle) / (1 + cos(angle)), -clamp, clamp)
	}

	fun vertex(pos: Vec2f) {
		val color = colorizer.getColor(0f, Vec3d(pos.x.toDouble(), pos.y.toDouble(), 0.0))
		buf.vertex(mat, pos.x, pos.y, 0f).color(ColorHelper.Argb.getRed(color), ColorHelper.Argb.getGreen(color), ColorHelper.Argb.getBlue(color), 1).next()
	}

	buf.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR)
	for (i in 0 until points.size - 1) {
		val p1 = points[i]
		val p2 = points[i + 1]
		val tangent = p2.add(p1.negate()).normalize().multiply(width * 0.5f)
		val normal = Vec2f(-tangent.y, tangent.x)

		val jlow = joinOffsets[i]
		val jhigh = joinOffsets[i + 1]
		val p1Down = p1.add(tangent.multiply(max(0f, jlow))).add(normal)
		val p1Up = p1.add(tangent.multiply(max(0f, -jlow))).add(normal.negate())
		val p2Down = p2.add(tangent.multiply(max(0f, jhigh)).negate()).add(normal)
		val p2Up = p2.add(tangent.multiply(max(0f, -jhigh)).negate()).add(normal.negate())

		vertex(p1Down)
		vertex(p1)
		vertex(p1Up)

		vertex(p1Down)
		vertex(p1Up)
		vertex(p2Up)

		vertex(p1Down)
		vertex(p2Up)
		vertex(p2)

		vertex(p1Down)
		vertex(p2)
		vertex(p2Down)

		if (i > 0) {
			val sangle = joinAngles[i]
			val angle = abs(sangle)
			val rnormal = normal.negate()
			val joinSteps: Int = ceil(angle * 180 / (CAP_THETA * MathHelper.PI)).toInt()
			if (joinSteps < 1) {
				continue
			}

			if (sangle < 0) {
				var prevVert = Vec2f(p1.x - rnormal.x, p1.y - rnormal.y)
				for (j in 1..joinSteps) {
					val fan = rotate(rnormal, -sangle * (j.toFloat() / joinSteps.toFloat()))
					val fanShift = Vec2f(p1.x - fan.x, p1.y - fan.y)
					vertex(p1)
					vertex(prevVert)
					vertex(fanShift)
					prevVert = fanShift
				}
			} else {
				val startFan = rotate(normal, -sangle)
				var prevVert = Vec2f(p1.x - startFan.x, p1.y - startFan.y)
				for (j in joinSteps - 1 downTo 0) {
					val fan = rotate(normal, -sangle * (j.toFloat() / joinSteps))
					val fanShift = Vec2f(p1.x - fan.x, p1.y - fan.y)

					vertex(p1)
					vertex(prevVert)
					vertex(fanShift)
					prevVert = fanShift
				}
			}
		}
	}
	RenderSystem.disableCull()
	tess.draw()
	RenderSystem.enableCull()
}