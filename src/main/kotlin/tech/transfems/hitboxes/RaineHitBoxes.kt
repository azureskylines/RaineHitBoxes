package tech.transfems.hitboxes

import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.potion.Potion
import net.minecraft.util.ChatComponentText
import net.weavemc.loader.api.command.Command
import net.weavemc.loader.api.command.CommandBus
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Paths

object RaineHitBoxes {

    private val cfgFile: File by lazy { Paths.get("RaineHitBoxes.config").toFile() }
    private val addChatMessage: (String) -> Unit = {
        Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(it))
    }

    var expandAmount: Double = 0.0
    var healthEnabled: Boolean = false
    var speedEnabled: Boolean = false

    private fun save() {
        if (!cfgFile.exists()) cfgFile.createNewFile()

        val config = arrayOf(expandAmount.toString(), healthEnabled.toString(), speedEnabled.toString())

        val writer = BufferedWriter(FileWriter(cfgFile))
        writer.write(config.joinToString("|"))
        writer.close()
    }

    private fun load() {
        if (!cfgFile.exists()) return

        try {
            val reader = BufferedReader(FileReader(cfgFile))
            val config = reader.lines().findFirst().get().split("|")
            reader.close()

            expandAmount = config[0].toDouble()
            healthEnabled = config[1].toBoolean()
            speedEnabled = config[2].toBoolean()
        } catch (e: Throwable) {
            println("[RaineHitBoxes] Failed to load config, resetting it :3")
            expandAmount = 0.0
            healthEnabled = false
            speedEnabled = false
        }
    }

    const val SYNTAX: String = "/rainehitboxes [speed|health|<expand amount>]"

    fun init() {
        println("[RaineHitBoxes] Cooked.")
        load()

        CommandBus.register(object : Command("rainehitboxes", "rhb") {
            override fun handle(args: Array<out String>) {
                if (args.size != 1) {
                    addChatMessage("Syntax: $SYNTAX")
                    return
                }

                when (args[0]) {
                    "health" -> {
                        healthEnabled = !healthEnabled
                        addChatMessage("Health sorting has been ${if (healthEnabled) "enabled" else "disabled"}.")
                    }
                    "speed" -> {
                        speedEnabled = !speedEnabled
                        addChatMessage("Speed II check has been ${if (speedEnabled) "enabled" else "disabled"}.")
                    }
                    else -> {
                        try {
                            val amount = args[0].toDoubleOrNull()

                            if (amount == null) {
                                addChatMessage("Error. Expand amount has to be a decimal number.")
                                return
                            }

                            if (amount < 0) {
                                addChatMessage("Error. Expand amount cannot be negative.")
                                return
                            }

                            expandAmount = amount

                            addChatMessage("HitBoxes are now expanded $expandAmount blocks.")

                            save()
                        } catch (e: Throwable) {
                            addChatMessage("Error. Syntax: $SYNTAX")
                        }
                    }
                }
            }
        })
    }

}

fun EntityLivingBase.potionAmp(potion: Potion): Int {
    return (this.getActivePotionEffect(potion) ?: return 0).amplifier + 1
}