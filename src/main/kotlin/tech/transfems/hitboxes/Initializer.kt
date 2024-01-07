package tech.transfems.hitboxes

import net.minecraft.client.Minecraft
import net.weavemc.loader.api.ModInitializer

class Initializer : ModInitializer {

    private val mc: Minecraft by lazy { Minecraft.getMinecraft() }

    override fun preInit() {
        RaineHitBoxes.init()
    }

}