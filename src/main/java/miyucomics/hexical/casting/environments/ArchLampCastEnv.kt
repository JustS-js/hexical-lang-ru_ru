package miyucomics.hexical.casting.environments

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand

class ArchLampCastEnv(caster: ServerPlayerEntity, castingHand: Hand, stack: ItemStack, finale: Boolean) : LampCastEnv(caster, castingHand, stack, finale) {
	override fun produceParticles(particles: ParticleSpray, pigment: FrozenPigment) {}

	override fun getCastingHand(): Hand = this.castingHand
	override fun getPigment(): FrozenPigment = IXplatAbstractions.INSTANCE.getPigment(this.caster)
}