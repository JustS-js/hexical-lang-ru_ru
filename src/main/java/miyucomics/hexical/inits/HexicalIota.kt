package miyucomics.hexical.inits

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import miyucomics.hexical.HexicalMain
import miyucomics.hexical.casting.iotas.DyeIota
import miyucomics.hexical.casting.iotas.IdentifierIota
import miyucomics.hexical.casting.iotas.PigmentIota
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object HexicalIota {
	private val TYPES: MutableMap<Identifier, IotaType<*>> = HashMap()
	val IDENTIFIER_IOTA = type("identifier", IdentifierIota.TYPE)
	val DYE_IOTA = type("dye", DyeIota.TYPE)
	val PIGMENT_IOTA = type("pigment", PigmentIota.TYPE)

	@JvmStatic
	fun init() {
		for ((key, value) in TYPES)
			Registry.register(HexIotaTypes.REGISTRY, key, value)
	}

	private fun <U : Iota, T : IotaType<U>> type(name: String, type: T): T {
		val old = TYPES.put(HexicalMain.id(name), type)
		require(old == null) { "Typo? Duplicate id $name" }
		return type
	}
}