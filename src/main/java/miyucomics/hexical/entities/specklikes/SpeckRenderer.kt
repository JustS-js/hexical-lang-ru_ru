package miyucomics.hexical.entities.specklikes

import at.petrak.hexcasting.api.HexAPI.modLoc
import com.mojang.blaze3d.systems.RenderSystem
import miyucomics.hexical.utils.RenderUtils
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d

class SpeckRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<SpeckEntity>(ctx) {
	override fun getTexture(entity: SpeckEntity?): Identifier? = null
	override fun shouldRender(entity: SpeckEntity?, frustum: Frustum?, x: Double, y: Double, z: Double) = true
	override fun render(entity: SpeckEntity?, yaw: Float, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {
		matrices.push()
		matrices.translate(0.0, 0.25, 0.0)
		if (entity!!.clientIsText)
			matrices.translate(0.0, 0.125, 0.0)
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-entity.yaw))
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.pitch))
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(entity.clientRoll))
		matrices.scale(entity.clientSize, entity.clientSize, entity.clientSize)

		val top = matrices.peek()
		if (entity.clientIsText) {
			RenderSystem.disableCull()
			val height = (-textRenderer.getWidth(entity.clientText) / 2).toFloat()
			matrices.scale(0.025f, -0.025f, 0.025f)
			textRenderer.draw(entity.clientText, height, 0f, -1, false, top.positionMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light)
			RenderSystem.enableCull()
		} else {
			val buffer = vertexConsumers.getBuffer(renderLayer)
			RenderUtils.drawLines(top.positionMatrix, top.normalMatrix, LightmapTextureManager.MAX_LIGHT_COORDINATE, entity.clientThickness * 0.05f / entity.clientSize, buffer, entity.clientVerts) { pos ->
				entity.clientPigment.colorProvider.getColor(0f, Vec3d(pos.x.toDouble(), pos.y.toDouble(), 0.0).multiply(2.0).add(entity.pos))
			}
		}

		matrices.pop()
	}

	companion object {
		private val renderLayer = RenderLayer.getEntityCutoutNoCull(modLoc("textures/entity/white.png"))
	}
}